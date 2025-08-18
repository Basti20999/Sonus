package dev.minceraft.sonus.svc.adapter.connection;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.udp.WrappedUdpPipelineData;
import dev.minceraft.sonus.svc.adapter.SvcProtocolAdapter;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcPlayerCipherCodec;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcUdpContext;
import dev.minceraft.sonus.svc.protocol.AbstractSvcPacket;
import dev.minceraft.sonus.svc.protocol.SvcUdpMagicCodec;
import dev.minceraft.sonus.svc.protocol.meta.SvcMetaPacket;
import dev.minceraft.sonus.svc.protocol.voice.SvcVoicePacket;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.net.InetSocketAddress;
import java.util.UUID;

@NullMarked
public class SvcConnection {

    private final SvcProtocolAdapter adapter;
    private final ISonusPlayer player;
    private final UUID secret = UUID.randomUUID();
    private final SvcPlayerCipherCodec cipher = new SvcPlayerCipherCodec(this.secret);
    private final VoiceHandler voiceHandler;
    private final MetaHandler metaHandler;
    // RemoteAddress will be set after first packet is received - usually at the construction of the connection
    private @MonotonicNonNull InetSocketAddress remoteAddress;
    private boolean connected = false;
    private long lastKeepAlive = System.currentTimeMillis();

    public SvcConnection(SvcProtocolAdapter adapter, ISonusPlayer player) {
        this.adapter = adapter;
        this.player = player;
        this.voiceHandler = new VoiceHandler(this.adapter, this);
        this.metaHandler = new MetaHandler(this.adapter, this);
    }

    public ISonusPlayer getPlayer() {
        return player;
    }

    public UUID getSecret() {
        return secret;
    }

    public void sendPacket(AbstractSvcPacket<?> packet) {
        if (packet instanceof SvcVoicePacket<?> voicePacket) {
            sendUdpPacket(voicePacket);
        } else if (packet instanceof SvcMetaPacket<?> metaPacket) {
            sendTcpPacket(metaPacket);
        } else {
            throw new IllegalArgumentException("Unsupported packet type: " + packet.getClass().getName());
        }
    }

    private void sendUdpPacket(SvcVoicePacket<?> packet) {
        if (this.remoteAddress == null) {
            throw new IllegalStateException("Cannot send UDP packet before remote address is set.");
        }
        WrappedUdpPipelineData payload = new WrappedUdpPipelineData(
                SvcUdpContext.newInstance(),
                this.remoteAddress,
                SvcUdpMagicCodec.INSTANCE,
                packet
        );
        this.adapter.getService().getUdpServer().sendPacket(payload);
    }

    private void sendTcpPacket(SvcMetaPacket<?> packet) {
        // TODO: Implement TCP/PM packet sending logic
    }

    public SvcPlayerCipherCodec getCipher() {
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

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public long getLastKeepAlive() {
        return this.lastKeepAlive;
    }

    public void setLastKeepAlive(long lastKeepAlive) {
        this.lastKeepAlive = lastKeepAlive;
    }
}
