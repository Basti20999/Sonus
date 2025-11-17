package dev.minceraft.sonus.agent.paper.api;
// Created by booky10 in Sonus (23:54 16.11.2025)

import dev.minceraft.sonus.agent.paper.SonusAgentPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
@NullMarked
public class SonusAgentApiImpl implements SonusAgentApi{

    protected final SonusAgentPlugin plugin;

    protected final Set<UUID> connectedPlayers = ConcurrentHashMap.newKeySet();

    public SonusAgentApiImpl(SonusAgentPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isConnected(Player player) {
        return this.connectedPlayers.contains(player.getUniqueId());
    }

    public Set<UUID> getConnectedPlayers() {
        return this.connectedPlayers;
    }
}
