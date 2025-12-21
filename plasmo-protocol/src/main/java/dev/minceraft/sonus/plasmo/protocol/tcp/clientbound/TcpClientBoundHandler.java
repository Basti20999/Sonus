package dev.minceraft.sonus.plasmo.protocol.tcp.clientbound;

public interface TcpClientBoundHandler {

    default void handleActivationRegisterPacket(ActivationRegisterPacket packet) {
    }

    default void handleActivationUnregisterPacket(ActivationUnregisterPacket packet) {
    }

    default void handleAnimatedActionBarPacket(AnimatedActionBarPacket packet) {
    }

    default void handleConfigPacket(ConfigPacket packet) {
    }

    default void handleConfigPlayerInfoPacket(ConfigPlayerInfoPacket<?> packet) {
    }

    default void handleConnectionPacket(ConnectionPacket packet) {
    }

    default void handleDistanceVisualizePacket(DistanceVisualizePacket packet) {
    }

    default void handleLanguagePacket(LanguagePacket packet) {
    }

    default void handlePlayerDisconnectPacket(PlayerDisconnectPacket packet) {
    }

    default void handlePlayerInfoRequestPacket(PlayerInfoRequestPacket packet) {
    }

    default void handlePlayerInfoUpdatePacket(PlayerInfoUpdatePacket packet) {
    }

    default void handlePlayerListPacket(PlayerListPacket packet) {
    }

    default void handleSelfSourceInfoPacket(SelfSourceInfoPacket packet) {
    }

    default void handleSourceAudioEndPacket(SourceAudioEndPacket packet) {
    }

    default void handleSourceInfoPacket(SourceInfoPacket packet) {
    }

    default void handleSourceLinePlayerAddPacket(SourceLinePlayerAddPacket packet) {
    }

    default void handleSourceLinePlayerRemovePacket(SourceLinePlayerRemovePacket packet) {
    }

    default void handleSourceLinePlayersListPacket(SourceLinePlayersListPacket packet) {
    }

    default void handleSourceLineRegisterPacket(SourceLineRegisterPacket packet) {
    }

    default void handleSourceLineUnregisterPacket(SourceLineUnregisterPacket packet) {
    }
}
