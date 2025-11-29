package dev.minceraft.sonus.svc.protocol.voice;

import dev.minceraft.sonus.svc.protocol.voice.clientbound.AuthenticateAckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.clientbound.ConnectionCheckAckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.clientbound.GroupSoundSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.clientbound.LocationSoundSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.clientbound.PlayerSoundSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.commonbound.KeepAliveSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.commonbound.PingSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.servicebound.AuthenticateSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.servicebound.ConnectionCheckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.servicebound.MicSvcPacket;

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
