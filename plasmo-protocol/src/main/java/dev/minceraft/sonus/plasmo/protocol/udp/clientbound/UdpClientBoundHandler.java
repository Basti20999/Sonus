package dev.minceraft.sonus.plasmo.protocol.udp.clientbound;

import dev.minceraft.sonus.plasmo.protocol.udp.bothbound.UdpBothBoundHandler;

public interface UdpClientBoundHandler extends UdpBothBoundHandler {

    default void handleSelfAudioPacket(SelfAudioInfoPlasmoPacket packet) {
    }

    default void handleSourceAudioPacket(SourceAudioPlasmoPacket packet) {
    }
}
