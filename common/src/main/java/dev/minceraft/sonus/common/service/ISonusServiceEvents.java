package dev.minceraft.sonus.common.service;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.UUID;

@NullMarked
public interface ISonusServiceEvents {

    default void onPlayerSwitchBackend(UUID playerId) {
    }

    default void onPlayerQuit(UUID playerId) {
    }

    default void onPlayerStateUpdate(ISonusPlayer player) {
    }

    default void onPlayerPositionUpdate(ISonusPlayer player) {
    }

    default void onChannelRegistered(UUID playerId, Set<Key> channel) {
    }

    default void onGroupLeave(IRoom room, UUID playerId) {
    }

    default void onGroupCreate(IRoom room) {
    }

    default void onGroupRemove(IRoom room) {
    }
}
