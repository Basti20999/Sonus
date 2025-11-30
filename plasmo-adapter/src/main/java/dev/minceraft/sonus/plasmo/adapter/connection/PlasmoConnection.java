package dev.minceraft.sonus.plasmo.adapter.connection;

import dev.minceraft.sonus.common.audio.AudioProcessor;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.udp.WrappedUdpPipelineData;
import dev.minceraft.sonus.plasmo.adapter.PlasmoAdapter;
import dev.minceraft.sonus.plasmo.adapter.pipeline.PlasmoUdpContext;
import dev.minceraft.sonus.plasmo.protocol.AbstractPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.PlasmoPmChannels;
import dev.minceraft.sonus.plasmo.protocol.cipher.CipherAes;
import dev.minceraft.sonus.plasmo.protocol.cipher.ICipher;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPacketRegistry;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.VoiceActivation;
import dev.minceraft.sonus.plasmo.protocol.udp.UdpPlasmoPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public class PlasmoConnection implements AutoCloseable {

    private final PlasmoAdapter adapter;
    private final ISonusPlayer player;
    private final UUID secret = UUID.randomUUID();

    private final MetaHandler metaHandler;
    private final VoiceHandler voiceHandler;
    private final Map<UUID, AudioProcessor> processors = new ConcurrentHashMap<>();

    private final Set<VoiceActivation> voiceActivations = new HashSet<>();
    private @MonotonicNonNull ICipher cipher;
    private @MonotonicNonNull InetSocketAddress remoteAddress;

    public PlasmoConnection(PlasmoAdapter adapter, ISonusPlayer player) {
        this.adapter = adapter;
        this.player = player;

        this.metaHandler = new MetaHandler(adapter, this);
        this.voiceHandler = new VoiceHandler(adapter, this);

        int voiceChatRange = (int) this.adapter.getService().getConfig().getVoiceChatRange();
        VoiceActivation defaultActivation = new VoiceActivation(
                "proximity",
                "pv.activation.proximity",
                "plasmovoice:textures/icons/microphone.png",
                List.of(voiceChatRange),
                voiceChatRange,
                true, // Allow proximity
                false, // Force mono audio
                true, // Allow other activations
                this.adapter.getUdpAdapter().getCodecInfo(),
                1 // weight
        );

        this.voiceActivations.add(defaultActivation);

        this.player.setAdapter(this.adapter);
    }

    public ICipher getCipher() {
        return this.cipher;
    }

    public void sendPacket(AbstractPlasmoPacket<?> packet) {
        if (packet instanceof UdpPlasmoPacket<?> voicePacket) {
            sendUdpPacket(voicePacket);
        } else if (packet instanceof TcpPlasmoPacket<?> metaPacket) {
            sendTcpPacket(metaPacket);
        } else {
            throw new IllegalArgumentException("Unsupported packet type: " + packet.getClass().getName());
        }
    }

    private void sendUdpPacket(UdpPlasmoPacket<?> packet) {
        if (this.remoteAddress == null) {
            throw new IllegalStateException("Cannot send UDP packet before remote address is set.");
        }
        packet.setSecret(this.getSecret());
        packet.setTimestamp(System.currentTimeMillis());
        WrappedUdpPipelineData payload = new WrappedUdpPipelineData(
                PlasmoUdpContext.newInstance(this.remoteAddress, this),
                this.adapter.getUdpAdapter().getPlasmoCodec(),
                packet
        );
        this.adapter.getService().getUdpServer().sendPacket(payload);
    }

    private void sendTcpPacket(TcpPlasmoPacket<?> packet) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            TcpPacketRegistry.REGISTRY.encode(buffer, packet);
            this.player.sendPluginMessage(PlasmoPmChannels.CHANNEL, buffer.retain());
        } finally {
            buffer.release();
        }
    }

    public ISonusPlayer getPlayer() {
        return this.player;
    }

    public UUID getSecret() {
        return this.secret;
    }

    public MetaHandler getMetaHandler() {
        return this.metaHandler;
    }

    public VoiceHandler getVoiceHandler() {
        return this.voiceHandler;
    }

    public Set<VoiceActivation> getVoiceActivations() {
        return this.voiceActivations;
    }

    public void initCipher(byte[] publicKey) {
        this.cipher = CipherAes.createFromRsaHandshake(publicKey);
    }

    public boolean isConnected() {
        return this.player.isConnected();
    }

    public void setConnected(boolean connected) {
        this.player.setConnected(connected);
    }

    public InetSocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public AudioProcessor getProcessor(UUID channelId) {
        return this.processors.computeIfAbsent(channelId, __ ->
                this.adapter.getService().createAudioProcessor(AudioProcessor.Mode.VOICE));
    }

    @Override
    public void close() {
        this.processors.values().removeIf(processor -> {
            processor.close();
            return true;
        });
    }
}
