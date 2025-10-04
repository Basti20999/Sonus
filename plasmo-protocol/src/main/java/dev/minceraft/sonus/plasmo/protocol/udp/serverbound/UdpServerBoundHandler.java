package dev.minceraft.sonus.plasmo.protocol.udp.serverbound;

import dev.minceraft.sonus.plasmo.protocol.udp.bothbound.UdpBothBoundHandler;

public interface UdpServerBoundHandler extends UdpBothBoundHandler {

    default void handlePlayerAudioPacket(PlayerAudioPlasmoPacket packet) {
    }
}
