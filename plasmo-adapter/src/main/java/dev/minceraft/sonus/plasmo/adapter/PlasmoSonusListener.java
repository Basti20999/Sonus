package dev.minceraft.sonus.plasmo.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.service.ISonusServiceEvents;
import dev.minceraft.sonus.plasmo.adapter.connection.PlasmoConnection;
import dev.minceraft.sonus.plasmo.protocol.PlasmoPmChannels;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.PlayerInfoRequestPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.PlayerInfoUpdatePacket;
import net.kyori.adventure.key.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

public class PlasmoSonusListener implements ISonusServiceEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final PlasmoAdapter adapter;

    public PlasmoSonusListener(PlasmoAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onPlayerQuit(UUID playerId) {
        ISonusServiceEvents.super.onPlayerQuit(playerId);
    }

    @Override
    public void onPlayerStateUpdate(ISonusPlayer player) {
        PlayerInfoUpdatePacket packet = new PlayerInfoUpdatePacket();
        packet.setPlayerInfo(this.adapter.buildPlayerInfo(player));

        this.adapter.getSessionManager().broadcastPacket(packet);
    }

    @Override
    public void onChannelRegistered(UUID playerId, Set<Key> channel) {
        if (!channel.contains(PlasmoPmChannels.CHANNEL)) {
            return;
        }

        PlasmoConnection connection = this.adapter.getSessionManager().createConnection(playerId);
        if (connection == null) {
            LOGGER.warn("Player '{}' registered plasmo channels, but this player is not known!", playerId);
            return;
        }

        PlayerInfoRequestPacket packet = new PlayerInfoRequestPacket();
        connection.sendPacket(packet);
    }
}
