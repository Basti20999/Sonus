package dev.minceraft.sonus.svc.adapter;

import com.google.common.collect.ImmutableMap;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import dev.minceraft.sonus.svc.protocol.AbstractSvcPacket;
import dev.minceraft.sonus.svc.protocol.data.SonusClientGroup;
import dev.minceraft.sonus.svc.protocol.data.SvcPlayerState;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.AddGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.JoinedGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.PlayerStatesSvcPacket;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_BYPASS_GROUP_PASSWORD;

@NullMarked
public class SvcSessionManager {

    private final SvcAdapter adapter;

    private final Map<UUID, SvcConnection> connections = new ConcurrentHashMap<>();

    public SvcSessionManager(SvcAdapter adapter) {
        this.adapter = adapter;
    }

    public void addConnection(SvcConnection connection) {
        this.connections.put(connection.getPlayer().getUniqueId(), connection);
    }

    public @Nullable SvcConnection getConnection(UUID playerId) {
        return this.connections.get(playerId);
    }

    public boolean removeSession(UUID playerId) {
        try (SvcConnection conn = this.connections.remove(playerId)) {
            return conn != null;
        }
    }

    public void onConnectionEstablished(SvcConnection connection) {
        // send group initialization packets for everything
        boolean bypassPassword = connection.getPlayer().hasPermission(PERMISSION_BYPASS_GROUP_PASSWORD, false);
        for (IRoom room : this.adapter.getService().getRoomManager().getRooms()) {
            AddGroupSvcPacket packet = new AddGroupSvcPacket();
            packet.setGroup(new SonusClientGroup(room, bypassPassword));
            connection.sendPacket(packet);
        }

        // broadcast new player state to everyone else
        connection.getPlayer().updateState();

        // bulk-initialize all player states
        PlayerStatesSvcPacket statesPacket = new PlayerStatesSvcPacket();
        statesPacket.setStates(this.buildBulkPlayerStates(connection.getPlayer()));
        connection.sendPacket(statesPacket);

        // primary room is still set, tell the player it's in a group
        IRoom primaryRoom = connection.getPlayer().getPrimaryRoom();
        if (primaryRoom != null) {
            JoinedGroupSvcPacket packet = new JoinedGroupSvcPacket();
            packet.setGroupId(primaryRoom.getId());
            packet.setWrongPassword(false);
            connection.sendPacket(packet);
        }
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

    public void broadcastFrom(ISonusPlayer source, AbstractSvcPacket<?> packet) {
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

    public SvcPlayerState buildPlayerState(ISonusPlayer viewer, ISonusPlayer player) {
        IRoom primaryRoom = player.getPrimaryRoom();
        return new SvcPlayerState(
                player.getUniqueId(viewer), player.getName(viewer),
                player.isDeafened(), !player.isConnected(),
                primaryRoom == null ? null : primaryRoom.getId()
        );
    }

    public Map<UUID, SvcPlayerState> buildBulkPlayerStates(ISonusPlayer player) {
        ImmutableMap.Builder<UUID, SvcPlayerState> states = ImmutableMap.builderWithExpectedSize(this.connections.size());
        for (ISonusPlayer target : this.adapter.getService().getPlayerManager().getPlayers()) {
            // build state update of target if player can see target
            if (target.isConnected() && player.canSee(target)) {
                states.put(target.getUniqueId(player), this.buildPlayerState(player, target));
            }
        }
        return states.build();
    }
}
