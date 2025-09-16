package dev.minceraft.sonus.service;

import dev.minceraft.sonus.common.service.ISonusServiceEvents;
import dev.minceraft.sonus.common.service.ISonusEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SonusEventManager implements ISonusEventManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final SonusService service;
    private final Set<ISonusServiceEvents> listeners = new HashSet<>();

    public SonusEventManager(SonusService service) {
        this.service = service;
    }

    @Override
    public void registerListener(ISonusServiceEvents events) {
        this.listeners.add(events);
    }

    @Override
    public void onPlayerSwitchBackend(UUID playerId) {
        for (ISonusServiceEvents listener : this.listeners) {
            try {
                listener.onPlayerSwitchBackend(playerId);
            } catch (Exception exception) {
                LOGGER.error("Error in onPlayerSwitchBackend for listener {}", listener.getClass().getSimpleName(), exception);
            }
        }
    }

    @Override
    public void onPlayerQuit(UUID playerId) {
        for (ISonusServiceEvents listener : this.listeners) {
            try {
                listener.onPlayerQuit(playerId);
            } catch (Exception exception) {
                LOGGER.error("Error in onPlayerQuit for listener {}", listener.getClass().getSimpleName(), exception);
            }
        }
        this.service.getPlayerManager().unregisterPlayer(playerId);
    }
}
