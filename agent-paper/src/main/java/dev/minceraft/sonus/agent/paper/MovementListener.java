package dev.minceraft.sonus.agent.paper;
// Created by booky10 in Sonus (01:39 17.07.2025)

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import dev.minceraft.sonus.protocol.meta.servicebound.PlayerPositionsMessage;
import dev.minceraft.sonus.util.WorldVec3d;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jspecify.annotations.NullMarked;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NullMarked
public class MovementListener implements Listener {

    private final SonusAgentPlugin plugin;
    private final Map<UUID, WorldVec3d> changedPos = new HashMap<>();

    public MovementListener(SonusAgentPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSpawnLocation(PlayerSpawnLocationEvent event) {
        this.onChange(event.getPlayer().getUniqueId(), event.getSpawnLocation());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        this.onMove(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (event.hasChangedPosition()) {
            this.onChange(event.getPlayer().getUniqueId(), event.getTo());
        }
    }

    private void onChange(UUID playerId, Location location) {
        NamespacedKey dimensionKey = location.getWorld().getKey();
        WorldVec3d pos = new WorldVec3d(location.getX(), location.getY(), location.getZ(), dimensionKey);
        this.changedPos.put(playerId, pos);
    }

    @EventHandler
    public void onTickEnd(ServerTickEndEvent event) {
        if (!this.changedPos.isEmpty()) {
            this.plugin.sendMetaPacket(new PlayerPositionsMessage(this.changedPos));
            this.changedPos.clear();
        }
    }
}
