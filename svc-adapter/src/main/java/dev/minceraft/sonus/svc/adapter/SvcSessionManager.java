package dev.minceraft.sonus.svc.adapter;

import com.google.common.collect.ImmutableMap;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import dev.minceraft.sonus.svc.protocol.AbstractSvcPacket;
import dev.minceraft.sonus.svc.protocol.data.SonusClientGroup;
import dev.minceraft.sonus.svc.protocol.data.SvcPlayerState;
import dev.minceraft.sonus.svc.protocol.meta.AddGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStateSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStatesSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.KeepAliveSvcPacket;
import net.kyori.adventure.util.TriState;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_BYPASS_GROUP_PASSWORD;

@NullMarked
public class SvcSessionManager {

    private final SvcAdapter adapter;

    private final Map<UUID, SvcConnection> connections = new ConcurrentHashMap<>();

    public SvcSessionManager(SvcAdapter adapter) {
        this.adapter = adapter;

        int keepAliveInterval = this.adapter.getService().getConfig().getKeepAliveMs();
        this.adapter.getService().getScheduler().schedule(
                this::tickKeepAlive, 0, keepAliveInterval, TimeUnit.MILLISECONDS);
    }

    public void addConnection(SvcConnection connection) {
        this.connections.put(connection.getPlayer().getUniqueId(), connection);
    }

    public @Nullable SvcConnection getConnection(UUID playerId) {
        return this.connections.get(playerId);
    }

    public void removeSession(UUID playerId) {
        this.connections.remove(playerId);
    }

    public void onConnectionEstablished(SvcConnection connection) {
        // broadcast new player state to everyone else
        connection.getPlayer().updateState();

        // bulk-initialize all player states
        PlayerStatesSvcPacket statesPacket = new PlayerStatesSvcPacket();
        statesPacket.setStates(this.buildBulkPlayerStates(connection.getPlayer()));
        connection.sendPacket(statesPacket);

        // send group initialization packets for everything
        boolean bypassPassword = connection.getPlayer().hasPermission(PERMISSION_BYPASS_GROUP_PASSWORD, false);
        for (IRoom room : this.adapter.getService().getRoomManager().getRooms()) {
            AddGroupSvcPacket packet = new AddGroupSvcPacket();
            packet.setGroup(new SonusClientGroup(room, bypassPassword));
            connection.sendPacket(packet);
        }
    }

    public void tickKeepAlive() {
        this.broadcast(KeepAliveSvcPacket.INSTANCE);
    }

    public void broadcast(AbstractSvcPacket<?> packet) {
        this.broadcast(__ -> packet);
    }

    public void broadcast(Function<SvcConnection, AbstractSvcPacket<?>> packet) {
        for (SvcConnection conn : this.connections.values()) {
            if (conn.isConnected()) {
                conn.sendPacket(packet.apply(conn));
            }
        }
    }

    public void broadcastFrom(ISonusPlayer source, PlayerStateSvcPacket packet) {
        this.broadcastFrom(source, __ -> packet);
    }

    public void broadcastFrom(ISonusPlayer source, Function<SvcConnection, AbstractSvcPacket<?>> packet) {
        for (SvcConnection conn : this.connections.values()) {
            if (!conn.isConnected() || !conn.getPlayer().canSee(source)) {
                continue; // target not connected or target can't see source
            }
            conn.getPlayer().ensureTabListed(source);
            conn.sendPacket(packet.apply(conn));
        }
    }

    public SvcPlayerState buildPlayerState(ISonusPlayer player, ISonusPlayer target) {
        IRoom primaryRoom = target.getPrimaryRoom();
        return new SvcPlayerState(
                target.getUniqueId(), target.getName(player),
                target.isDeafened(), !target.isConnected(),
                primaryRoom == null ? null : primaryRoom.getId()
        );
    }

    public Map<UUID, SvcPlayerState> buildBulkPlayerStates(ISonusPlayer player) {
        ImmutableMap.Builder<UUID, SvcPlayerState> states = ImmutableMap.builderWithExpectedSize(this.connections.size());
        for (ISonusPlayer target : this.adapter.getService().getPlayerManager().getPlayers()) {
            // build state update of target if player can see target
            if (target.isConnected() && player.canSee(target)) {
                states.put(target.getUniqueId(), this.buildPlayerState(player, target));
            }
        }
        return states.build();
    }
}
