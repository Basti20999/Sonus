package dev.minceraft.sonus.svc.adapter.connection;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.svc.protocol.voice.AuthenticateAckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.AuthenticateSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.ConnectionCheckAckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.ConnectionCheckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.GroupSoundSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.IVoiceSvcHandler;
import dev.minceraft.sonus.svc.protocol.voice.KeepAliveSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.LocationSoundSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.MicSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.PingSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.PlayerSoundSvcPacket;

public class VoiceHandler implements IVoiceSvcHandler {

    @Override
    public void handleAuthenticateAck(ISonusPlayer player, AuthenticateAckSvcPacket packet) {
        IVoiceSvcHandler.super.handleAuthenticateAck(player, packet);
    }

    @Override
    public void handleAuthenticate(ISonusPlayer player, AuthenticateSvcPacket packet) {
        IVoiceSvcHandler.super.handleAuthenticate(player, packet);
    }

    @Override
    public void handleConnectionCheckAck(ISonusPlayer player, ConnectionCheckAckSvcPacket packet) {
        IVoiceSvcHandler.super.handleConnectionCheckAck(player, packet);
    }

    @Override
    public void handleConnectionCheck(ISonusPlayer player, ConnectionCheckSvcPacket packet) {
        IVoiceSvcHandler.super.handleConnectionCheck(player, packet);
    }

    @Override
    public void handleGroupSoundPacket(ISonusPlayer player, GroupSoundSvcPacket packet) {
        IVoiceSvcHandler.super.handleGroupSoundPacket(player, packet);
    }

    @Override
    public void handleKeepAlivePacket(ISonusPlayer player, KeepAliveSvcPacket packet) {
        IVoiceSvcHandler.super.handleKeepAlivePacket(player, packet);
    }

    @Override
    public void handleLocationSoundPacket(ISonusPlayer player, LocationSoundSvcPacket packet) {
        IVoiceSvcHandler.super.handleLocationSoundPacket(player, packet);
    }

    @Override
    public void handleMicPacket(ISonusPlayer player, MicSvcPacket packet) {
        IVoiceSvcHandler.super.handleMicPacket(player, packet);
    }

    @Override
    public void handlePingPacket(ISonusPlayer player, PingSvcPacket packet) {
        IVoiceSvcHandler.super.handlePingPacket(player, packet);
    }

    @Override
    public void handlePlayerSoundPacket(ISonusPlayer player, PlayerSoundSvcPacket packet) {
        IVoiceSvcHandler.super.handlePlayerSoundPacket(player, packet);
    }
}
