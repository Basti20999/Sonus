package dev.minceraft.sonus.protocol.svc.voice;

import dev.minceraft.sonus.common.data.ISonusPlayer;

public interface IVoiceSvcHandler {

    default void handleAuthenticateAck(ISonusPlayer player, AuthenticateAckSvcPacket packet) {
    }

    default void handleAuthenticate(ISonusPlayer player, AuthenticateSvcPacket packet) {
    }

    default void handleConnectionCheckAck(ISonusPlayer player, ConnectionCheckAckSvcPacket packet) {
    }

    default void handleConnectionCheck(ISonusPlayer player, ConnectionCheckSvcPacket packet) {
    }

    default void handleGroupSoundPacket(ISonusPlayer player, GroupSoundSvcPacket packet) {
    }

    default void handleKeepAlivePacket(ISonusPlayer player, KeepAliveSvcPacket packet) {
    }

    default void handleLocationSoundPacket(ISonusPlayer player, LocationSoundSvcPacket packet) {
    }

    default void handleMicPacket(ISonusPlayer player, MicSvcPacket packet) {
    }

    default void handlePingPacket(ISonusPlayer player, PingSvcPacket packet) {
    }

    default void handlePlayerSoundPacket(ISonusPlayer player, PlayerSoundSvcPacket packet) {
    }
}
