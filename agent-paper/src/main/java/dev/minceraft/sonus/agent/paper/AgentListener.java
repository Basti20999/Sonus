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
import org.bukkit.event.player.PlayerRegisterChannelEvent;
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

import static dev.minceraft.sonus.common.SonusConstants.PLUGIN_MESSAGE_CHANNEL;

@NullMarked
public class AgentListener implements Listener {

    protected final SonusAgentPlugin plugin;

    protected final Map<UUID, WorldVec3d> positionUpdates = new HashMap<>();
    protected final Table<UUID, UUID, SonusPlayerState> playerStates = HashBasedTable.create();
    protected final Table<UUID, UUID, SonusPlayerState> playerStateUpdates = HashBasedTable.create();
    protected final Set<Map.Entry<Player, Player>> visibilityChanges = new HashSet<>();

    protected final Map<UUID, @Nullable String> teams = new HashMap<>();
    protected final Map<UUID, @Nullable String> teamUpdates = new HashMap<>();

    protected boolean dirtyPlayerMeta = true;
    protected boolean dirtyRoomDefinition = true;

    public AgentListener(SonusAgentPlugin plugin) {
        this.plugin = plugin;
    }

    protected SonusPlayerState buildState(Player player, Player target) {
        boolean staticHidden = !player.canSee(target);
        boolean spatialHidden = staticHidden
                || target.getGameMode() == GameMode.SPECTATOR
                && player.getGameMode() != GameMode.SPECTATOR;
        return new SonusPlayerState(target.getUniqueId(), staticHidden, spatialHidden);
    }

    protected boolean isPlayerIgnored(Player player) {
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        // specify initial player position
        Player player = event.getPlayer();
        this.onChangePos(player, player.getLocation());

        // set default player states for everything
        UUID playerId = player.getUniqueId();
        Map<UUID, SonusPlayerState> playerRow = this.playerStates.row(playerId);
        Map<UUID, SonusPlayerState> playerColumn = this.playerStates.column(playerId);
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player == target || this.isPlayerIgnored(target)) {
                continue;
            }
            UUID targetId = target.getUniqueId();
            playerRow.computeIfAbsent(targetId,
                    __ -> this.buildState(player, target));
            playerColumn.computeIfAbsent(targetId,
                    __ -> this.buildState(target, player));
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

        // re-send room definition to service if everyone quits
        int playerCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!this.isPlayerIgnored(player)) {
                if (++playerCount > 1) {
                    break;
                }
            }
        }
        if (playerCount <= 1) {
            this.dirtyRoomDefinition = true;
        }

        this.plugin.getApi().getConnectedPlayers().remove(playerId);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        this.onMove(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (event.hasChangedPosition()) {
            this.onChangePos(event.getPlayer(), event.getTo());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGamemode(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (event.getNewGameMode() == GameMode.SPECTATOR
                || player.getGameMode() == GameMode.SPECTATOR) {
            // TODO better solution for this
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (player != target && !this.isPlayerIgnored(target)) {
                    this.visibilityChanges.add(Map.entry(player, target));
                    this.visibilityChanges.add(Map.entry(target, player));
                }
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

    public void onChangePos(Player player, Location location) {
        NamespacedKey dimensionKey = location.getWorld().getKey();
        double posY = location.getY() + player.getEyeHeight();
        WorldVec3d pos = new WorldVec3d(location.getX(), posY, location.getZ(), dimensionKey);
        this.positionUpdates.put(player.getUniqueId(), pos);
        this.dirtyPlayerMeta = true;
    }

    public void tickTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.isPlayerIgnored(player)) {
                continue;
            }
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
            SonusPlayerState state = this.buildState(entry.getKey(), entry.getValue());
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
        if (this.dirtyRoomDefinition) {
            this.dirtyRoomDefinition = false;
            this.plugin.getLogger().info("Sending sonus room definition to service");

            UpdateRoomDefinitionMessage packet = new UpdateRoomDefinitionMessage();
            packet.setDefinition(roomDefinition);
            this.plugin.sendMetaPacket(packet);
        }
    }

    // try to send room definition only after a channel has been registered
    @EventHandler
    public void onAgentChannelRegistration(PlayerRegisterChannelEvent event) {
        if (PLUGIN_MESSAGE_CHANNEL.equals(event.getChannel())) {
            // try fire room definition for this server room to service, as
            // we don't have a connection to the service if there is no player connected
            this.sendRoomDefinition();
        }
    }
}
