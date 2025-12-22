package dev.minceraft.sonus.plasmo.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.service.ISonusServiceEvents;
import dev.minceraft.sonus.plasmo.adapter.connection.PlasmoConnection;
import dev.minceraft.sonus.plasmo.protocol.PlasmoPmChannels;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.PlayerDisconnectPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.PlayerInfoRequestPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.PlayerInfoUpdatePacket;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

@NullMarked
public class PlasmoSonusListener implements ISonusServiceEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final PlasmoAdapter adapter;

    public PlasmoSonusListener(PlasmoAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onPlayerSwitchBackend(UUID playerId) {
        // remove backend-specific session
        if (this.adapter.getSessionManager().removeSession(playerId)) {
            // disable sonus during backend switch
            this.adapter.getService().getPlayerManager().disableOnBackendSwitch(playerId);
        }
    }

    @Override
    public void onPlayerDisconnect(ISonusPlayer player) {
        // remove backend-specific session
        this.adapter.getSessionManager().removeSession(player.getUniqueId());

        // completely remove player
        PlayerDisconnectPacket packet = new PlayerDisconnectPacket();
        packet.setUniqueId(player.getUniqueId());
        this.adapter.getSessionManager().broadcast(packet);
    }

    @Override
    public void onPlayerStateUpdate(ISonusPlayer player, boolean globalUpdate) {
        this.adapter.getSessionManager().broadcastFrom(player, !globalUpdate, connection -> {
            PlayerInfoUpdatePacket packet = new PlayerInfoUpdatePacket();
            packet.setPlayerInfo(this.adapter.getSessionManager().buildPlayerInfo(connection.getPlayer(), player));

            return packet;
        });
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
