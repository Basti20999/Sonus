package dev.minceraft.sonus.agent.paper;
// Created by booky10 in Sonus (01:39 17.07.2025)

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.common.rooms.options.RoomDefinition;
import dev.minceraft.sonus.protocol.meta.servicebound.BackendTickMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.UpdateRoomDefinitionMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerHideEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerShowEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@NullMarked
public class AgentListener implements Listener {

    private final SonusAgentPlugin plugin;
    private final Map<UUID, WorldVec3d> changedPos = new HashMap<>();
    private final Multimap<UUID, SonusPlayerState> hiddenPlayers = HashMultimap.create();

    private final Map<UUID, @Nullable String> teams = new HashMap<>();
    private final Map<UUID, @Nullable String> teamsDiff = new HashMap<>();
    private boolean roomDefinitionSent = false;

    public AgentListener(SonusAgentPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.sendRoomDefinition();
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
        this.setVisibility(event.getPlayer().getUniqueId(), event.getEntity(),
                true, true);
    }

    @EventHandler
    public void onShow(PlayerShowEntityEvent event) {
        this.setVisibility(event.getPlayer().getUniqueId(), event.getEntity(),
                false, false);
    }

    private void setVisibility(UUID playerId, Entity target, boolean tablistHidden, boolean hidden) {
        if (target instanceof Player) {
            this.hiddenPlayers.put(playerId, new SonusPlayerState(target.getUniqueId(), tablistHidden, hidden));
        }
    }

    private void onChangePos(UUID playerId, Location location) {
        NamespacedKey dimensionKey = location.getWorld().getKey();
        WorldVec3d pos = new WorldVec3d(location.getX(), location.getY(), location.getZ(), dimensionKey);
        this.changedPos.put(playerId, pos);
    }

    private void tickTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Team playerTeam = scoreboard.getPlayerTeam(player);
            String name = playerTeam == null ? null : playerTeam.getName();

            String old = this.teams.put(player.getUniqueId(), name);
            if (!Objects.equals(old, name)) {
                this.teamsDiff.put(player.getUniqueId(), name);
            }
        }
    }

    @EventHandler
    public void onTickEnd(ServerTickEndEvent event) {
        this.tickTeams();
        if (this.changedPos.isEmpty() &&
                this.hiddenPlayers.isEmpty() &&
                this.teamsDiff.isEmpty()

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
        if (!this.teamsDiff.isEmpty()) {
            packet.setTeams(this.teamsDiff);
        }

        this.plugin.sendMetaPacket(packet);

        this.changedPos.clear();
        this.hiddenPlayers.clear();
        this.teamsDiff.clear();
    }

    private void sendRoomDefinition() {
        RoomDefinition roomDefinition = this.plugin.getRoomDefinition();
        if (roomDefinition == null) {
            return;
        }
        if (this.roomDefinitionSent) {
            return;
        }
        UpdateRoomDefinitionMessage packet = new UpdateRoomDefinitionMessage();
        packet.setDefinition(roomDefinition);
        this.plugin.sendMetaPacket(packet);

        this.roomDefinitionSent = true;
    }
}
