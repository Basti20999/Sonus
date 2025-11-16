package dev.minceraft.sonus.agent.paper;
// Created by booky10 in Sonus (01:39 17.07.2025)

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.common.rooms.options.RoomDefinition;
import dev.minceraft.sonus.protocol.meta.servicebound.BackendTickMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.UpdateRoomDefinitionMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerHideEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShowEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@NullMarked
public class AgentListener implements Listener {

    private final SonusAgentPlugin plugin;

    private final Map<UUID, WorldVec3d> positionUpdates = new HashMap<>();
    private final Table<UUID, UUID, SonusPlayerState> playerStates = HashBasedTable.create();
    private final Table<UUID, UUID, SonusPlayerState> playerStateUpdates = HashBasedTable.create();
    private final Set<Map.Entry<Player, Player>> visibilityChanges = new HashSet<>();

    private final Map<UUID, @Nullable String> teams = new HashMap<>();
    private final Map<UUID, @Nullable String> teamUpdates = new HashMap<>();

    private boolean dirtyPlayerMeta = true;
    private boolean dirtyRoomDefinition = true;

    public AgentListener(SonusAgentPlugin plugin) {
        this.plugin = plugin;
    }

    public static SonusPlayerState buildState(Player player, Player target) {
        boolean fullyHidden = !player.canSee(target);
        boolean hidden = fullyHidden
                || target.getGameMode() == GameMode.SPECTATOR
                && player.getGameMode() != GameMode.SPECTATOR;
        return new SonusPlayerState(target.getUniqueId(), fullyHidden, hidden);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        // try fire room definition for this server room to service, as
        // we don't have a connection to the service if there is no player connected
        this.sendRoomDefinition();

        // specify initial player position
        Player player = event.getPlayer();
        this.onChangePos(player.getUniqueId(), player.getLocation());

        // set default player states for everything
        UUID playerId = player.getUniqueId();
        Map<UUID, SonusPlayerState> playerRow = this.playerStates.row(playerId);
        Map<UUID, SonusPlayerState> playerColumn = this.playerStates.column(playerId);
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player == target) {
                continue;
            }
            UUID targetId = target.getUniqueId();
            playerRow.computeIfAbsent(targetId,
                    __ -> buildState(player, target));
            playerColumn.computeIfAbsent(targetId,
                    __ -> buildState(target, player));
        }
        this.playerStateUpdates.row(playerId).putAll(playerRow);
        this.playerStateUpdates.column(playerId).putAll(playerColumn);
        this.dirtyPlayerMeta = true;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // remove cached data for player
        UUID playerId = event.getPlayer().getUniqueId();
        this.playerStates.rowMap().remove(playerId);
        this.playerStates.columnMap().remove(playerId);
        this.playerStateUpdates.rowMap().remove(playerId);
        this.playerStateUpdates.columnMap().remove(playerId);
        this.teams.remove(playerId);
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGamemode(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (event.getNewGameMode() == GameMode.SPECTATOR
                || player.getGameMode() == GameMode.SPECTATOR) {
            // TODO better solution for this
            for (Player target : Bukkit.getOnlinePlayers()) {
                this.visibilityChanges.add(Map.entry(player, target));
                this.visibilityChanges.add(Map.entry(target, player));
            }
        }
    }

    @EventHandler
    public void onHide(PlayerHideEntityEvent event) {
        if (event.getEntity() instanceof Player target) {
            this.visibilityChanges.add(Map.entry(event.getPlayer(), target));
        }
    }

    @EventHandler
    public void onShow(PlayerShowEntityEvent event) {
        if (event.getEntity() instanceof Player target) {
            this.visibilityChanges.add(Map.entry(event.getPlayer(), target));
        }
    }

    public void onChangePos(UUID playerId, Location location) {
        NamespacedKey dimensionKey = location.getWorld().getKey();
        WorldVec3d pos = new WorldVec3d(location.getX(), location.getY(), location.getZ(), dimensionKey);
        this.positionUpdates.put(playerId, pos);
        this.dirtyPlayerMeta = true;
    }

    public void tickTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Team playerTeam = scoreboard.getPlayerTeam(player);
            String name = playerTeam == null ? null : playerTeam.getName();

            String old = this.teams.put(player.getUniqueId(), name);
            if (!Objects.equals(old, name)) {
                this.teamUpdates.put(player.getUniqueId(), name);
                this.dirtyPlayerMeta = true;
            }
        }
    }

    public void tickVisibilityChanges() {
        Iterator<Map.Entry<Player, Player>> it = this.visibilityChanges.iterator();
        while (it.hasNext()) {
            Map.Entry<Player, Player> entry = it.next();
            it.remove();
            SonusPlayerState state = buildState(entry.getKey(), entry.getValue());
            this.playerStates.put(entry.getKey().getUniqueId(), state.playerId(), state);
            this.playerStateUpdates.put(entry.getKey().getUniqueId(), state.playerId(), state);
            this.dirtyPlayerMeta = true;
        }
    }

    @EventHandler
    public void onTickEnd(ServerTickEndEvent event) {
        this.tickVisibilityChanges();
        this.tickTeams();
        this.tickDirtyPlayerMeta();
    }

    public void tickDirtyPlayerMeta() {
        if (!this.dirtyPlayerMeta) {
            return;
        }
        this.dirtyPlayerMeta = false;

        BackendTickMessage packet = new BackendTickMessage();
        if (!this.positionUpdates.isEmpty()) {
            packet.setPositions(this.positionUpdates);
        }
        if (!this.playerStateUpdates.isEmpty()) {
            packet.setPerPlayerStates(this.playerStateUpdates);
        }
        if (!this.teamUpdates.isEmpty()) {
            packet.setTeams(this.teamUpdates);
        }
        this.plugin.sendMetaPacket(packet);

        this.positionUpdates.clear();
        this.playerStateUpdates.clear();
        this.teamUpdates.clear();
    }

    private void sendRoomDefinition() {
        RoomDefinition roomDefinition = this.plugin.getRoomDefinition();
        if (roomDefinition == null) {
            return;
        }
        if (!this.dirtyRoomDefinition) {
            this.dirtyRoomDefinition = true;
            UpdateRoomDefinitionMessage packet = new UpdateRoomDefinitionMessage();
            packet.setDefinition(roomDefinition);
            this.plugin.sendMetaPacket(packet);
        }
    }
}
