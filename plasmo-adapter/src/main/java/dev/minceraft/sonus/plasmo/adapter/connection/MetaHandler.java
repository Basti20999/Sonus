package dev.minceraft.sonus.plasmo.adapter.connection;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.plasmo.adapter.PlasmoAdapter;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.ConnectionPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SourceInfoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.source.SourceInfo;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.LanguageRequestPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.PlayerActivationDistancesPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.PlayerAudioEndPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.PlayerInfoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.PlayerStatePacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.SourceInfoRequestPacket;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class MetaHandler implements TcpHandler {

    private final PlasmoAdapter adapter;
    private final PlasmoConnection connection;

    public MetaHandler(PlasmoAdapter adapter, PlasmoConnection connection) {
        this.adapter = adapter;
        this.connection = connection;
    }

    @Override
    public void handleLanguageRequestPacket(LanguageRequestPacket packet) {

    }

    @Override
    public void handlePlayerActivationDistancesPacket(PlayerActivationDistancesPacket packet) {

    }

    @Override
    public void handlePlayerAudioEndPacket(PlayerAudioEndPacket packet) {
        this.connection.getPlayer().handleAudioInputEnd(packet.getSequenceNumber());
    }

    @Override
    public void handlePlayerInfoPacket(PlayerInfoPacket packet) {
        this.connection.initCipher(packet.getPublicKey());
        ISonusPlayer player = this.connection.getPlayer();
        player.setDeafened(packet.isVoiceDisabled());
        player.setMuted(packet.isMicrophoneMuted());

        ConnectionPacket connectionPacket = new ConnectionPacket();
        connectionPacket.setSecret(this.connection.getSecret());
        connectionPacket.setInetAddress(this.adapter.getService().getUdpServer().getRemoteAddress());

        this.connection.sendPacket(connectionPacket);
    }

    @Override
    public void handlePlayerStatePacket(PlayerStatePacket<?> packet) {
        this.connection.getPlayer().setDeafened(packet.isVoiceDisabled());
        this.connection.getPlayer().setMuted(packet.isMicrophoneMuted());
        this.connection.getPlayer().updateState();
    }

    @Override
    public void handleSourceInfoRequestPacket(SourceInfoRequestPacket packet) {
        UUID sourceId = packet.getSourceId();

        SourceInfo sourceInfo = this.connection.getSourceInfo(sourceId);
        if (sourceInfo == null) {
            return;
        }

        SourceInfoPacket infoPacket = new SourceInfoPacket();
        infoPacket.setSourceInfo(sourceInfo);
        this.connection.sendPacket(infoPacket);
    }
}
