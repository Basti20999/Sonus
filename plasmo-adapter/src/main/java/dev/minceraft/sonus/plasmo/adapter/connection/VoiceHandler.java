package dev.minceraft.sonus.plasmo.adapter.connection;

import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.plasmo.adapter.PlasmoAdapter;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.ConfigPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.PlayerListPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.CaptureInfo;
import dev.minceraft.sonus.plasmo.protocol.udp.UdpHandler;
import dev.minceraft.sonus.plasmo.protocol.udp.bothbound.CustomPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.udp.bothbound.PingPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.udp.serverbound.PlayerAudioPlasmoPacket;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class VoiceHandler implements UdpHandler {

    private final PlasmoAdapter adapter;
    private final PlasmoConnection connection;

    public VoiceHandler(PlasmoAdapter adapter, PlasmoConnection connection) {
        this.adapter = adapter;
        this.connection = connection;
    }

    @Override
    public void handlePlayerAudioPacket(PlayerAudioPlasmoPacket packet) {
        short[] pcm = this.connection.getProcessor().decode(packet.getAudioData());
        this.connection.getPlayer().handleAudioInput(new SonusAudio.Pcm(pcm, packet.getSequenceNumber()));
    }

    @Override
    public void handleCustomPacket(CustomPlasmoPacket packet) {

    }

    @Override
    public void handlePingPacket(PingPlasmoPacket packet) {
        if (!this.connection.isConnected()) { // Init connection
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
            configPacket.setSourceLines(Set.of());
            configPacket.setActivations(this.connection.getVoiceActivations());

            this.connection.sendPacket(configPacket);

            this.connection.setConnected(true);
            this.connection.getPlayer().handleConnect();

            PlayerListPacket playerListPacket = new PlayerListPacket();
            playerListPacket.setPlayers(List.copyOf(this.adapter.getSessionManager().getPlayerInfos(this.connection).values()));

            this.connection.sendPacket(playerListPacket);
        }
    }
}
