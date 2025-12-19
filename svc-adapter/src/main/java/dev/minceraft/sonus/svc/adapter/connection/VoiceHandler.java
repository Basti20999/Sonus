package dev.minceraft.sonus.svc.adapter.connection;

import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.svc.adapter.SvcProtocolAdapter;
import dev.minceraft.sonus.svc.protocol.voice.IVoiceSvcHandler;
import dev.minceraft.sonus.svc.protocol.voice.clientbound.AuthenticateAckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.clientbound.ConnectionCheckAckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.commonbound.KeepAliveSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.commonbound.PingSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.servicebound.AuthenticateSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.servicebound.ConnectionCheckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.servicebound.MicSvcPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class VoiceHandler implements IVoiceSvcHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");
    private static final UUID MIC_CHANNEL_ID = new UUID(9018035903106730674L, -6405133132459802568L);

    private final SvcProtocolAdapter protocolAdapter;
    private final SvcConnection connection;
    private State state = State.WAITING_AUTH;

    public VoiceHandler(SvcProtocolAdapter protocolAdapter, SvcConnection connection) {
        this.protocolAdapter = protocolAdapter;
        this.connection = connection;
    }

    @Override
    public void handleAuthenticate(AuthenticateSvcPacket packet) {
        if (this.state != State.WAITING_AUTH) {
            return;
        }
        if (!packet.getSecret().equals(this.connection.getSecret())) {
            return; // Ignore packets with mismatched secret
        }
        if (!packet.getPlayerId().equals(this.connection.getPlayer().getUniqueId(this.connection.getPlayer()))) {
            LOGGER.warn("Received AuthenticateSvcPacket for player {} with mismatched ID: {}.",
                    this.connection.getPlayer().getUniqueId(), packet.getPlayerId());
            return;
        }
        this.connection.sendPacket(new AuthenticateAckSvcPacket());
        this.state = State.WAITING_CHECK;
    }

    @Override
    public void handleConnectionCheck(ConnectionCheckSvcPacket packet) {
        if (this.state != State.WAITING_CHECK) {
            return;
        }
        LOGGER.info("Successfully connected {}({}) to Sonus SVC backend - version {}",
                this.connection.getPlayer().getName(), this.connection.getPlayer().getUniqueId(), this.connection.getVersion());

        this.connection.getPlayer().setKeepAlive(System.currentTimeMillis());
        this.connection.setConnected(true);
        this.connection.getPlayer().setMuted(false);
        this.connection.getPlayer().setDeafened(false);
        this.protocolAdapter.getAdapter().getSessions().onConnectionEstablished(this.connection);

        this.connection.sendPacket(new ConnectionCheckAckSvcPacket());

        this.connection.getPlayer().handleConnect();
        this.state = State.CONNECTED;
    }

    @Override
    public void handleKeepAlivePacket(KeepAliveSvcPacket packet) {
        if (this.state == State.CONNECTED) {
            this.connection.getPlayer().setKeepAlive(System.currentTimeMillis());
        }
    }

    @Override
    public void handleMicPacket(MicSvcPacket packet) {
        if (this.state != State.CONNECTED) {
            return;
        }

        byte[] raw = packet.getData();
        if (raw.length == 0) { // Audio end
            this.connection.getPlayer().handleAudioInputEnd(packet.getSequenceNumber());
            return;
        }

        short[] pcm = this.connection.getProcessor(MIC_CHANNEL_ID).decode(raw);
        SonusAudio data = new SonusAudio.Pcm(pcm, packet.getSequenceNumber());
        this.connection.getPlayer().handleAudioInput(data);
    }

    @Override
    public void handlePingPacket(PingSvcPacket packet) {
        // NO-OP, we never send ping packets
    }

    public enum State {
        WAITING_AUTH,
        WAITING_CHECK,
        CONNECTED,
    }
}
