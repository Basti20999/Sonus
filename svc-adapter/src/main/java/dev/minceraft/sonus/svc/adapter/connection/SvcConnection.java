package dev.minceraft.sonus.svc.adapter.connection;

import dev.minceraft.sonus.common.audio.AudioProcessor;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.tcp.holder.PmDataHolderBuf;
import dev.minceraft.sonus.common.protocol.udp.WrappedUdpPipelineData;
import dev.minceraft.sonus.svc.adapter.SvcProtocolAdapter;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcPlayerCipherCodec;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcUdpContext;
import dev.minceraft.sonus.svc.protocol.AbstractSvcPacket;
import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.meta.SvcMetaPacket;
import dev.minceraft.sonus.svc.protocol.registries.SvcMetaPacketRegistry;
import dev.minceraft.sonus.svc.protocol.voice.SvcVoicePacket;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public class SvcConnection implements AutoCloseable {

    private final SvcProtocolAdapter protocolAdapter;
    private final ISonusPlayer player;
    private final UUID secret = UUID.randomUUID();

    private final VoiceHandler voiceHandler;
    private final MetaHandler metaHandler;

    private @Nullable SvcPlayerCipherCodec cipher;
    private final Map<UUID, AudioProcessor> processors = new ConcurrentHashMap<>();

    // Pending pings keyed by ping ID → sent timestamp (ms)
    private final Map<UUID, Long> pendingPings = new ConcurrentHashMap<>();

    // RemoteAddress will be set after first packet is received - usually at the construction of the connection
    private @MonotonicNonNull InetSocketAddress remoteAddress;
    private SvcPacketContext ctx = SvcPacketContext.INITIAL;

    private @Nullable UUID currentRoomId; // Needed for group self join updates

    public SvcConnection(SvcProtocolAdapter protocolAdapter, ISonusPlayer player) {
        this.protocolAdapter = protocolAdapter;
        this.player = player;
        this.voiceHandler = new VoiceHandler(this.protocolAdapter, this);
        this.metaHandler = new MetaHandler(this.protocolAdapter, this);
    }

    public ISonusPlayer getPlayer() {
        return player;
    }

    public UUID getSecret() {
        return secret;
    }

    public void sendPacket(AbstractSvcPacket<?> packet) {
        if (packet instanceof SvcVoicePacket voicePacket) {
            sendUdpPacket(voicePacket);
        } else if (packet instanceof SvcMetaPacket metaPacket) {
            sendTcpPacket(metaPacket);
        } else {
            throw new IllegalArgumentException("Unsupported packet type: " + packet.getClass().getName());
        }
    }

    private void sendUdpPacket(SvcVoicePacket packet) {
        if (this.remoteAddress == null) {
            throw new IllegalStateException("Cannot send UDP packet before remote address is set.");
        }
        WrappedUdpPipelineData payload = new WrappedUdpPipelineData(
                SvcUdpContext.newInstance(this.remoteAddress, this),
                this.protocolAdapter.getSvcCodec(),
                packet
        );
        this.protocolAdapter.getAdapter().getService().getUdpServer().sendPacket(payload);
    }

    private void sendTcpPacket(SvcMetaPacket packet) {
        Key channel = packet.getPluginMessageChannel().getForVersion(this.ctx.version());
        if (channel == null) {
            return;
        }
        PmDataHolderBuf data = PmDataHolderBuf.newInstance(channel);
        try {
            SvcMetaPacketRegistry.BUF_REGISTRY.encode(data, packet, this.ctx);
            this.player.sendPluginMessage(data.getSecond(), data.getFirst().retain());
        } finally {
            data.recycle();
        }
    }

    public SvcPlayerCipherCodec getCipher() {
        if (this.cipher == null) {
            throw new IllegalStateException("Cipher not initialized yet. Make sure to set the protocol version first.");
        }
        return this.cipher;
    }

    public VoiceHandler getVoiceHandler() {
        return this.voiceHandler;
    }

    public MetaHandler getMetaHandler() {
        return this.metaHandler;
    }

    public InetSocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public boolean isVoiceActive() {
        return this.player.isVoiceActive();
    }

    public void setVoiceActive(boolean active) {
        this.player.setVoiceActive(active);
    }

    public boolean isDisabled() {
        return this.player.isDeafened();
    }

    /**
     * @return true if the state was changed, false if it was the same as before
     */
    public boolean setDisabled(boolean disabled) {
        if (this.player.isDeafened() == disabled) {
            return false;
        }
        this.player.setDeafened(disabled);
        return true;
    }

    public int getVersion() {
        return this.ctx.version();
    }

    public void setVersion(int version) {
        this.ctx = this.ctx.withVersion(version);
        // Cipher depends on the version, so we need to recreate it
        this.cipher = new SvcPlayerCipherCodec(this, this.protocolAdapter.getSvcCodec(), this.secret);
    }

    public SvcPacketContext getContext() {
        return this.ctx;
    }

    public AudioProcessor getProcessor(UUID channelId) {
        return this.processors.computeIfAbsent(channelId, __ ->
                this.protocolAdapter.getAdapter().getService().createAudioProcessor(AudioProcessor.Mode.VOICE));
    }

    @Nullable
    public UUID getCurrentRoomId() {
        return this.currentRoomId;
    }

    public void setCurrentRoomId(@Nullable UUID currentRoomId) {
        this.currentRoomId = currentRoomId;
    }

    /**
     * Records the send time of an outbound ping so RTT can be calculated on reply.
     */
    public void trackSentPing(UUID pingId, long sentTimeMs) {
        this.pendingPings.put(pingId, sentTimeMs);
    }

    /**
     * Called when a ping packet is received. If the ID matches an outbound ping we sent,
     * the RTT is calculated and forwarded to the player; returns {@code true} in that case.
     * Returns {@code false} if the ping was client-initiated and should be echoed back.
     */
    public boolean onPingReceived(UUID pingId, long nowMs) {
        Long sentTime = this.pendingPings.remove(pingId);
        if (sentTime == null) {
            return false; // client-initiated ping
        }
        this.player.setVoicePing(nowMs - sentTime);
        return true;
    }

    @Override
    public void close() {
        this.pendingPings.clear();
        this.processors.values().removeIf(processor -> {
            processor.close();
            return true;
        });
    }
}
