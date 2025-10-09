package dev.minceraft.sonus.plasmo.adapter.connection;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.plasmo.adapter.PlasmoAdapter;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.ConnectionPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.LanguageRequestPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.PlayerActivationDistancesPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.PlayerAudioEndPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.PlayerInfoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.PlayerStatePacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.SourceInfoRequestPacket;
import org.jspecify.annotations.NullMarked;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

@NullMarked
public class MetaHandler implements TcpHandler {

    private static final KeyFactory RSA_KEY_FACTORY;

    static {
        try {
            RSA_KEY_FACTORY = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }
    }

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

        this.adapter.getService().getEventManager().onPlayerStateUpdate(this.connection.getPlayer());
    }

    @Override
    public void handleSourceInfoRequestPacket(SourceInfoRequestPacket packet) {

    }
}
