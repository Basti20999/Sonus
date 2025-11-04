package dev.minceraft.sonus.agent.paper;
// Created by booky10 in Sonus (01:39 17.07.2025)

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.protocol.meta.servicebound.BackendTickMessage;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerHideEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerShowEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jspecify.annotations.NullMarked;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NullMarked
public class AgentListener implements Listener {

    private final SonusAgentPlugin plugin;
    private final Map<UUID, WorldVec3d> changedPos = new HashMap<>();
    private final Multimap<UUID, SonusPlayerState> hiddenPlayers = HashMultimap.create();
    private final Map<UUID, String> teams = new HashMap<>();

    public AgentListener(SonusAgentPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSpawnLocation(PlayerSpawnLocationEvent event) {
        this.onChangePos(event.getPlayer().getUniqueId(), event.getSpawnLocation());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        this.onMove(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (event.hasChangedPosition()) {
            this.onChangePos(event.getPlayer().getUniqueId(), event.getTo());
        }
    }

    @EventHandler
    public void onHide(PlayerHideEntityEvent event) {
        this.setVisibility(event.getPlayer().getUniqueId(), event.getEntity(), true);
    }

    @EventHandler
    public void onShow(PlayerShowEntityEvent event) {
        this.setVisibility(event.getPlayer().getUniqueId(), event.getEntity(), false);
    }

    private void setVisibility(UUID playerId, Entity target, boolean hidden) {
        if (!(target instanceof Player)) {
            return;
        }
        this.hiddenPlayers.put(playerId, new SonusPlayerState(target.getUniqueId(), hidden));
    }

    private void onChangePos(UUID playerId, Location location) {
        NamespacedKey dimensionKey = location.getWorld().getKey();
        WorldVec3d pos = new WorldVec3d(location.getX(), location.getY(), location.getZ(), dimensionKey);
        this.changedPos.put(playerId, pos);
    }

    @EventHandler
    public void onTickEnd(ServerTickEndEvent event) {
        if (this.changedPos.isEmpty() &&
                this.hiddenPlayers.isEmpty() &&
                this.teams.isEmpty()

        ) { // Pre-check to avoid allocating empty packets on main thread
            return;
        }
        BackendTickMessage packet = new BackendTickMessage();
        if (!this.changedPos.isEmpty()) {
            packet.setPositions(this.changedPos);
        }
        if (!this.hiddenPlayers.isEmpty()) {
            packet.setPerPlayerStates(this.hiddenPlayers);
        }
        if (!this.teams.isEmpty()){
            packet.setTeams(this.teams);
        }

        this.plugin.sendMetaPacket(packet);

        this.changedPos.clear();
        this.hiddenPlayers.clear();
        this.teams.clear();
    }
}
