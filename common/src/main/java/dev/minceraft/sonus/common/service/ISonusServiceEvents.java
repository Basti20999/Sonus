package dev.minceraft.sonus.common.service;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import net.kyori.adventure.key.Key;

import java.util.Set;
import java.util.UUID;

public interface ISonusServiceEvents {

    default void onPlayerSwitchBackend(UUID playerId) {
    }

    default void onPlayerQuit(UUID playerId) {
    }

    default void onPlayerStateUpdate(ISonusPlayer player) {
    }

    default void onChannelRegistered(UUID playerId, Set<Key> channel) {
    }
}
