package dev.minceraft.sonus.common.events;

import java.util.UUID;

public interface IServiceEvents {

    default void onPlayerSwitchBackend(UUID playerId) {
    }

    default void onPlayerQuit(UUID playerId) {
    }
}
