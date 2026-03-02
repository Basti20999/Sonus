package dev.minceraft.sonus.web.adapter.rtc;
// Created by booky10 in Sonus (4:26 AM 02.03.2026)

import dev.minceraft.sonus.web.adapter.config.WebConfig;
import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.PortAllocatorConfig;
import dev.onvoid.webrtc.RTCConfiguration;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.logging.Logging;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NullMarked
public final class RtcManager implements AutoCloseable {

    static {
        RtcSlf4jLogger.register(Logging.Severity.INFO, "WebRtc");
    }

    private final PeerConnectionFactory factory = new PeerConnectionFactory();
    private final RTCConfiguration config = new RTCConfiguration();

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

    public RtcHandler getPeer(WebSocketConnection connection) {
        UUID playerId = connection.getPlayer().getUniqueId();
        return this.peers.computeIfAbsent(playerId, __ -> this.createPeer(connection));
    }

    public RtcHandler createPeer(WebSocketConnection signalConnection) {
        RtcHandler handler = new RtcHandler(signalConnection);
        RTCPeerConnection connection = this.factory.createPeerConnection(this.config, handler);
        handler.setPeer(connection);
        handler.prepareOffer();
        return handler;
    }

    @Override
    public void close() {
        this.factory.dispose();
    }
}
