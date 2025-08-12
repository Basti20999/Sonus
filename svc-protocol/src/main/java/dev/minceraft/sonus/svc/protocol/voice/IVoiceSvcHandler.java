package dev.minceraft.sonus.svc.protocol.voice;

public interface IVoiceSvcHandler {

    default void handleAuthenticateAck(AuthenticateAckSvcPacket packet) {
    }

    default void handleAuthenticate(AuthenticateSvcPacket packet) {
    }

    default void handleConnectionCheckAck(ConnectionCheckAckSvcPacket packet) {
    }

    default void handleConnectionCheck(ConnectionCheckSvcPacket packet) {
    }

    default void handleGroupSoundPacket(GroupSoundSvcPacket packet) {
    }

    default void handleKeepAlivePacket(KeepAliveSvcPacket packet) {
    }

    default void handleLocationSoundPacket(LocationSoundSvcPacket packet) {
    }

    default void handleMicPacket(MicSvcPacket packet) {
    }

    default void handlePingPacket(PingSvcPacket packet) {
    }

    default void handlePlayerSoundPacket(PlayerSoundSvcPacket packet) {
    }
}
