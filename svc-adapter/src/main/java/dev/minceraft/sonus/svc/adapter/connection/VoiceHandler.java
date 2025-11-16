package dev.minceraft.sonus.svc.adapter.connection;

import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.svc.adapter.SvcProtocolAdapter;
import dev.minceraft.sonus.svc.protocol.voice.AuthenticateAckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.AuthenticateSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.ConnectionCheckAckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.ConnectionCheckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.IVoiceSvcHandler;
import dev.minceraft.sonus.svc.protocol.voice.KeepAliveSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.MicSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.PingSvcPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceHandler implements IVoiceSvcHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final SvcProtocolAdapter protocolAdapter;
    private final SvcConnection connection;

    public VoiceHandler(SvcProtocolAdapter protocolAdapter, SvcConnection connection) {
        this.protocolAdapter = protocolAdapter;
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
        this.connection.sendPacket(new AuthenticateAckSvcPacket());
    }

    @Override
    public void handleConnectionCheck(ConnectionCheckSvcPacket packet) {
        LOGGER.info("Successfully connected {}({}) to Sonus SVC backend - version {}",
                this.connection.getPlayer().getName(), this.connection.getPlayer().getUniqueId(), this.connection.getVersion());

        this.connection.setLastKeepAlive(System.currentTimeMillis());
        this.connection.setConnected(true);
        this.connection.getPlayer().setMuted(false);
        this.connection.getPlayer().setDeafened(false);
        this.protocolAdapter.getAdapter().getSessions().onConnectionEstablished(this.connection);

        this.connection.sendPacket(new ConnectionCheckAckSvcPacket());

        this.connection.getPlayer().handleConnect();
    }

    @Override
    public void handleKeepAlivePacket(KeepAliveSvcPacket packet) {
        this.connection.setLastKeepAlive(System.currentTimeMillis());
    }

    @Override
    public void handleMicPacket(MicSvcPacket packet) {
        short[] pcm = this.connection.getProcessor().decode(packet.getData());
        SonusAudio data = new SonusAudio(pcm, packet.getSequenceNumber());
        this.connection.getPlayer().handleAudioInput(data);
    }

    @Override
    public void handlePingPacket(PingSvcPacket packet) {
        // TODO: handle ping packet
    }
}
