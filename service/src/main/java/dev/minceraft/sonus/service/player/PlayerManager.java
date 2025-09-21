package dev.minceraft.sonus.service.player;
// Created by booky10 in Sonus (02:18 17.07.2025)

import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public final class PlayerManager {

    private final SonusService service;
    private final Map<UUID, SonusPlayer> players = new ConcurrentHashMap<>();

    public PlayerManager(SonusService service) {
        this.service = service;
    }

    public boolean unregisterPlayer(UUID playerId) {
        SonusPlayer removed = this.players.remove(playerId);
        if (removed == null) {
            return false;
        }
        removed.handleQuit();

        return true;
    }

    public @Nullable SonusPlayer getPlayer(UUID playerId) {
        SonusPlayer player = this.players.get(playerId);
        if (player == null) {
            IPlatformPlayer platform = this.service.getPlatform().getPlayer(playerId);
            if (platform != null) {
                player = new SonusPlayer(this.service, platform);
                this.players.put(playerId, player);
            }
        }

        return player;
    }
}
