package dev.minceraft.sonus.web.adapter.rtc;
// Created by booky10 in Sonus (4:26 AM 02.03.2026)

import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.web.adapter.config.WebConfig;
import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.PortAllocatorConfig;
import dev.onvoid.webrtc.RTCConfiguration;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.logging.Logging;
import dev.onvoid.webrtc.media.SyncClock;
import dev.onvoid.webrtc.media.audio.AudioProcessing;
import dev.onvoid.webrtc.media.audio.AudioProcessingStreamConfig;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@NullMarked
public final class RtcManager implements AutoCloseable {

    static {
        RtcSlf4jLogger.register(Logging.Severity.INFO, "WebRtc");
    }

    private final PeerConnectionFactory factory = new PeerConnectionFactory();
    private final RTCConfiguration config = new RTCConfiguration();
    private final AudioProcessing processor = new AudioProcessing();
    private final SyncClock clock = new SyncClock();

    private final ScheduledExecutorService audioTicker = Executors.newScheduledThreadPool(1);
    private final Map<UUID, RtcHandler> peers = new HashMap<>();

    public RtcManager(WebConfig config) {
        // configure ICE STUN/TURN servers
        config.iceServers.stream()
                .map(WebConfig.IceServerConfig::create)
                .forEach(this.config.iceServers::add);

        // apply network config
        WebConfig.RtcNetworkConfig netConf = config.rtcNetwork;
        PortAllocatorConfig allocConf = this.config.portAllocatorConfig;
        allocConf.minPort = netConf.minPort;
        allocConf.maxPort = netConf.maxPort;
        allocConf.setDisableTcp(!netConf.enableTcp);
        allocConf.setDisableUdp(!netConf.enableUdp);
        allocConf.setEnableIpv6(netConf.enableIpv6);
        allocConf.setEnableIpv6OnWifi(netConf.enableIpv6);
        allocConf.setDisableStun(!netConf.enableStun);
        allocConf.setDisableRelay(!netConf.enableRelay);
        allocConf.setDisableUdpRelay(!netConf.enableRelay);
    }

    public @Nullable RtcHandler getPeer(UUID playerId) {
        return this.peers.get(playerId);
    }

    public RtcHandler getPeer(WebSocketConnection connection) {
        UUID playerId = connection.getPlayer().getUniqueId();
        return this.peers.computeIfAbsent(playerId, __ -> this.createPeer(connection));
    }

    public RtcHandler createPeer(WebSocketConnection signalConnection) {
        RtcHandler handler = new RtcHandler(this, signalConnection);
        RTCPeerConnection connection = this.factory.createPeerConnection(this.config, handler);
        handler.setPeer(connection);
        handler.initialize(this.factory, this.audioTicker);
        return handler;
    }

    public byte[] resampleAudio(byte[] src, int sampleRate, int channels) {
        AudioProcessingStreamConfig inputConf = new AudioProcessingStreamConfig(sampleRate, channels);
        AudioProcessingStreamConfig outputConf = new AudioProcessingStreamConfig(SonusConstants.SAMPLE_RATE, SonusConstants.CHANNELS);
        byte[] dst = sampleRate == SonusConstants.SAMPLE_RATE && channels == SonusConstants.CHANNELS ? src :
                new byte[this.processor.getTargetBufferSize(inputConf, outputConf)];

        int errorCode = this.processor.processStream(src, inputConf, outputConf, dst);
        if (errorCode != 0) {
            throw new IllegalStateException("Received error while processing audio stream: " + errorCode);
        }
        return dst;
    }

    public SyncClock getClock() {
        return this.clock;
    }

    @Override
    public void close() {
        this.factory.dispose();
        this.processor.dispose();
        this.peers.values().removeIf(handler -> {
            handler.disconnect("manager closed");
            return true;
        });
        this.clock.dispose();
        this.audioTicker.close();
    }
}
