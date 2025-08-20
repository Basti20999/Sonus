package dev.minceraft.sonus.common.service;

import java.util.UUID;

public interface ISonusServiceEvents {

    default void onPlayerSwitchBackend(UUID playerId) {
    }

    default void onPlayerQuit(UUID playerId) {
    }
}
