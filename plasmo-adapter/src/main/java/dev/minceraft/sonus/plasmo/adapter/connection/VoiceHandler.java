package dev.minceraft.sonus.plasmo.adapter.connection;

import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.plasmo.adapter.PlasmoAdapter;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.ConfigPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.PlayerListPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.CaptureInfo;
import dev.minceraft.sonus.plasmo.protocol.udp.UdpHandler;
import dev.minceraft.sonus.plasmo.protocol.udp.bothbound.PingPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.udp.serverbound.PlayerAudioPlasmoPacket;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Map;

@NullMarked
public class VoiceHandler implements UdpHandler {

    private final PlasmoAdapter adapter;
    private final PlasmoConnection connection;

    public VoiceHandler(PlasmoAdapter adapter, PlasmoConnection connection) {
        this.adapter = adapter;
        this.connection = connection;
    }

    @Override
    public void handlePlayerAudioPacket(PlayerAudioPlasmoPacket packet) {
        short[] pcm = this.connection.getProcessor(packet.getActivationId()).decode(packet.getAudioData());
        this.connection.getPlayer().handleAudioInput(new SonusAudio.Pcm(pcm, packet.getSequenceNumber()));
    }

    @Override
    public void handlePingPacket(PingPlasmoPacket packet) {
        if (!this.connection.isConnected()) { // Init connection
            this.sendConfig();
            this.connection.setConnected(true);

            this.connection.getPlayer().handleConnect();

            this.sendPlayerList();
        }
        this.connection.getPlayer().setKeepAlive(System.currentTimeMillis());
    }

    private void sendConfig() {
        ConfigPacket configPacket = new ConfigPacket();
        configPacket.setPermissions(Map.of());
        configPacket.setServerId(this.adapter.getConfig().getDelegate().serverId);

        CaptureInfo captureInfo = new CaptureInfo(
                SonusConstants.SAMPLE_RATE,
                this.adapter.getService().getConfig().getMtuSize(),
                this.adapter.getUdpAdapter().getCodecInfo()
        );
        configPacket.setCaptureInfo(captureInfo);

        configPacket.setEncryptionInfo(this.connection.getCipher().getEncryptionInfo());
        configPacket.setSourceLines(this.connection.getSourceLines().values());
        configPacket.setActivations(this.connection.getVoiceActivations().values());

        this.connection.sendPacket(configPacket);
    }

    private void sendPlayerList() {
        PlayerListPacket playerListPacket = new PlayerListPacket();
        playerListPacket.setPlayers(List.copyOf(this.adapter.getSessionManager().getPlayerInfos(this.connection).values()));

        this.connection.sendPacket(playerListPacket);
    }
}
