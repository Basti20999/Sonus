package dev.minceraft.sonus.svc.adapter.connection;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceHandler implements IVoiceSvcHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final SvcConnection connection;

    public VoiceHandler(SvcConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleAuthenticate(AuthenticateSvcPacket packet) {
        if (!packet.getSecret().equals(this.connection.getSecret())) {
            return; // Ignore packets with mismatched secret
        }
        if (!packet.getPlayerId().equals(this.connection.getPlayer().getUniqueId())) {
            LOGGER.warn("Received AuthenticateSvcPacket for player {} with mismatched ID: {}.",
                    this.connection.getPlayer().getUniqueId(), packet.getPlayerId());
            return;
        }
        this.connection.sendPacket(new AuthenticateSvcPacket());
    }

    @Override
    public void handleConnectionCheckAck(ConnectionCheckAckSvcPacket packet) {
        IVoiceSvcHandler.super.handleConnectionCheckAck(packet);
    }

    @Override
    public void handleConnectionCheck(ConnectionCheckSvcPacket packet) {
        IVoiceSvcHandler.super.handleConnectionCheck(packet);
    }

    @Override
    public void handleGroupSoundPacket(GroupSoundSvcPacket packet) {
        IVoiceSvcHandler.super.handleGroupSoundPacket(packet);
    }

    @Override
    public void handleKeepAlivePacket(KeepAliveSvcPacket packet) {
        IVoiceSvcHandler.super.handleKeepAlivePacket(packet);
    }

    @Override
    public void handleLocationSoundPacket(LocationSoundSvcPacket packet) {
        IVoiceSvcHandler.super.handleLocationSoundPacket(packet);
    }

    @Override
    public void handleMicPacket(MicSvcPacket packet) {
        IVoiceSvcHandler.super.handleMicPacket(packet);
    }

    @Override
    public void handlePingPacket(PingSvcPacket packet) {
        IVoiceSvcHandler.super.handlePingPacket(packet);
    }

    @Override
    public void handlePlayerSoundPacket(PlayerSoundSvcPacket packet) {
        IVoiceSvcHandler.super.handlePlayerSoundPacket(packet);
    }
}
