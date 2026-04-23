package dev.minceraft.sonus.service;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.service.ISonusEventManager;
import dev.minceraft.sonus.common.service.ISonusServiceEvents;
import dev.minceraft.sonus.service.player.SonusPlayer;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@NullMarked
public final class SonusEventManager implements ISonusEventManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final SonusService service;
    private final Set<ISonusServiceEvents> listeners = ConcurrentHashMap.newKeySet();

    public SonusEventManager(SonusService service) {
        this.service = service;
    }

    @Override
    public void registerListener(ISonusServiceEvents events) {
        this.listeners.add(events);
    }

    @Override
    public void unregisterListener(ISonusServiceEvents events) {
        this.listeners.remove(events);
    }

    private void fire(String name, Consumer<ISonusServiceEvents> action) {
        for (ISonusServiceEvents listener : this.listeners) {
            try {
                action.accept(listener);
            } catch (Exception exception) {
                LOGGER.error("Error in {} for listener {}", name, listener.getClass().getSimpleName(), exception);
            }
        }
    }

    @Override
    public void onPlayerSwitchBackend(UUID playerId) {
        this.fire("onPlayerSwitchBackend", l -> l.onPlayerSwitchBackend(playerId));
        this.service.getPlayerManager().onPlayerSwitchBackend(playerId);
    }

    @Override
    public void onPlayerDisconnect(ISonusPlayer player) {
        this.fire("onPlayerDisconnect", l -> l.onPlayerDisconnect(player));
    }

    @Override
    public void onPlayerQuit(ISonusPlayer player) {
        this.fire("onPlayerQuit", l -> l.onPlayerQuit(player));
        this.service.getPlayerManager().unregisterPlayer(player.getUniqueId());
    }

    @Override
    public void onPlayerNickUpdate(ISonusPlayer player, UUID previousNick) {
        this.fire("onPlayerNickUpdate", l -> l.onPlayerNickUpdate(player, previousNick));
    }

    @Override
    public void onPlayerStateUpdate(ISonusPlayer player, boolean globalUpdate) {
        this.fire("onPlayerStateUpdate", l -> l.onPlayerStateUpdate(player, globalUpdate));
    }

    @Override
    public void onChannelRegistered(UUID playerId, Set<Key> channel) {
        Set<Key> snapshot = Set.copyOf(channel);
        this.fire("onChannelRegistered", l -> l.onChannelRegistered(playerId, snapshot));
    }

    @Override
    public void onPrimaryRoomJoined(ISonusPlayer player, IRoom room) {
        this.fire("onPrimaryRoomJoined", l -> l.onPrimaryRoomJoined(player, room));
    }

    @Override
    public void onPrimaryRoomLeaved(ISonusPlayer player, IRoom room) {
        this.fire("onPrimaryRoomLeaved", l -> l.onPrimaryRoomLeaved(player, room));
    }

    @Override
    public void onGroupCreate(IRoom room) {
        this.fire("onGroupCreate", l -> l.onGroupCreate(room));
    }

    @Override
    public void onGroupRemove(IRoom room) {
        this.fire("onGroupRemove", l -> l.onGroupRemove(room));
    }

    @Override
    public void onConnectionState(ISonusPlayer player) {
        this.fire("onConnectionState", l -> l.onConnectionState(player));
        ((SonusPlayer) player).updateCommands();
    }

    @Override
    public void onPlayerVisibilityStateUpdate(ISonusPlayer player, ISonusPlayer target, SonusPlayerState state) {
        this.fire("onPlayerVisibilityStateUpdate", l -> l.onPlayerVisibilityStateUpdate(player, target, state));
    }
}
