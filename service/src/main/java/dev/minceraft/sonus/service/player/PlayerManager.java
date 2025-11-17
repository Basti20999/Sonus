package dev.minceraft.sonus.service.player;
// Created by booky10 in Sonus (02:18 17.07.2025)

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.minceraft.sonus.common.IPlayerManager;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import dev.minceraft.sonus.service.platform.IServer;
import dev.minceraft.sonus.service.server.SonusServer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public final class PlayerManager implements IPlayerManager {

    private final SonusService service;
    private final Map<UUID, SonusPlayer> players = new ConcurrentHashMap<>();
    private final LoadingCache<UUID, SonusServer> serverCache;

    public PlayerManager(SonusService service) {
        this.service = service;

        this.serverCache = CacheBuilder.newBuilder()
                .weakValues().ticker(Ticker.systemTicker())
                .build(new CacheLoader<>() {
                    @Override
                    public SonusServer load(UUID serverId) {
                        IServer server = service.getPlatform().getServer(serverId);
                        return new SonusServer(service, server);
                    }
                });
    }

    public boolean unregisterPlayer(UUID playerId) {
        try (SonusPlayer removed = this.players.remove(playerId)) {
            if (removed == null) {
                return false;
            }
            removed.handleQuit();
            return true;
        }
    }

    @Override
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

    @Override
    public Collection<SonusPlayer> getPlayers() {
        return this.players.values();
    }

    public SonusServer getServer(UUID serverId) {
        return this.serverCache.getUnchecked(serverId);
    }

    public void onPlayerSwitchBackend(UUID playerId) {
        SonusPlayer player = this.getPlayer(playerId);
        if (player == null) {
            return;
        }
        player.setStates(Map.of()); // reset player states on server switch
        player.setConnected(false, false); // prevent packet sending
        player.setMuted(true);
        player.setDeafened(true);
        player.updateState(); // broadcast update

        for (SonusPlayer target : this.players.values()) {
            if (target.getPrimaryRoom() == null || target == player) {
                continue; // no primary room set, ignore
            }
            // show skin of target for this player
            if (player.canSee(target)) {
                player.ensureTabListed(target);
            }
            // show skin of this player to target (if this player is in primary room)
            if (player.getPrimaryRoom() != null && target.canSee(player)) {
                target.ensureTabListed(player);
            }
        }

        player.updateServer();
    }
}
