package dev.minceraft.sonus.plasmo.adapter;

import dev.minceraft.sonus.common.service.ISonusServiceEvents;
import dev.minceraft.sonus.plasmo.adapter.connection.PlasmoConnection;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.PlayerInfoRequestPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlasmoSonusListener implements ISonusServiceEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final PlasmoAdapter adapter;

    public PlasmoSonusListener(PlasmoAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onPlayerSwitchBackend(UUID playerId) {
        PlasmoConnection connection = this.adapter.getSessionManager().createConnection(playerId);
        if (connection == null) {
            LOGGER.warn("Player '{}' switched Server, but this player is not known!", playerId);
            return;
        }

        this.adapter.getService().getScheduler().schedule(() -> { // TODO: Remove this delay
            PlayerInfoRequestPacket packet = new PlayerInfoRequestPacket();
            connection.sendPacket(packet);
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onPlayerQuit(UUID playerId) {
        ISonusServiceEvents.super.onPlayerQuit(playerId);
    }
}
