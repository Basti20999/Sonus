package dev.minceraft.sonus.plasmo.protocol.udp.bothbound;

public interface UdpBothBoundHandler {

    default void handleBaseAudioPacket(BaseAudioPlasmoPacket packet) {
    }

    default void handleCustomPacket(CustomPlasmoPacket packet) {
    }

    default void handlePingPacket(PingPlasmoPacket packet) {
    }
}
