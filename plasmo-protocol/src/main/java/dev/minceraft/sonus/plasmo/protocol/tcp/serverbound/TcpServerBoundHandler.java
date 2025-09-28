package dev.minceraft.sonus.plasmo.protocol.tcp.serverbound;

public interface TcpServerBoundHandler {

    default void handleLanguageRequestPacket(LanguageRequestPacket packet) {
    }

    default void handlePlayerActivationDistancesPacket(PlayerActivationDistancesPacket packet) {
    }

    default void handlePlayerAudioEndPacket(PlayerAudioEndPacket packet) {
    }

    default void handlePlayerInfoPacket(PlayerInfoPacket packet) {
    }

    default void handlePlayerStatePacket(PlayerStatePacket packet) {
    }

    default void handleSourceInfoRequestPacket(SourceInfoRequestPacket packet) {
    }
}
