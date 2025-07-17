package dev.minceraft.service.player;
// Created by booky10 in Sonus (02:18 17.07.2025)

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public final class PlayerManager {

    private final Map<UUID, SonusPlayer> players = new ConcurrentHashMap<>();

    public void registerPlayer(UUID playerId) {
        SonusPlayer existingPlayer = this.players.putIfAbsent(playerId, new SonusPlayer(playerId));
        if (existingPlayer != null) {
            throw new IllegalArgumentException("Player " + playerId + " is already registered");
        }
    }

    public boolean unregisterPlayer(UUID playerId) {
        return this.players.remove(playerId) != null;
    }

    public @Nullable SonusPlayer getPlayer(UUID playerId) {
        return this.players.get(playerId);
    }
}
