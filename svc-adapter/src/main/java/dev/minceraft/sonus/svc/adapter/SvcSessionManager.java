package dev.minceraft.sonus.svc.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import dev.minceraft.sonus.svc.protocol.AbstractSvcPacket;
import dev.minceraft.sonus.svc.protocol.data.SonusClientGroup;
import dev.minceraft.sonus.svc.protocol.data.SvcPlayerState;
import dev.minceraft.sonus.svc.protocol.meta.AddGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStateSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStatesSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemoveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.KeepAliveSvcPacket;
import net.kyori.adventure.util.TriState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_BYPASS_GROUP_PASSWORD;

public class SvcSessionManager {

    private final SvcAdapter adapter;
    private final Map<UUID, SvcConnection> connections = new HashMap<>();

    public SvcSessionManager(SvcAdapter adapter) {
        this.adapter = adapter;

        this.adapter.getService().getScheduler().schedule(this::tickKeepAlive,
                0,
                this.adapter.getService().getConfig().getKeepAliveMs(),
                TimeUnit.MILLISECONDS);
    }

    public SvcConnection getConnection(UUID playerId) {
        return this.connections.get(playerId);
    }

    public void addConnection(SvcConnection connection) {
        this.connections.put(connection.getPlayer().getUniqueId(), connection);
    }

    public void onConnectionEstablished(SvcConnection connection) {
        this.adapter.getService().getEventManager().onPlayerStateUpdate(connection.getPlayer());

        PlayerStatesSvcPacket statesPacket = new PlayerStatesSvcPacket();
        statesPacket.setStates(this.getPlayerStates(connection));
        connection.sendPacket(statesPacket);

        boolean bypassPassword = connection.getPlayer().hasPermission(PERMISSION_BYPASS_GROUP_PASSWORD, TriState.NOT_SET);
        for (IRoom room : this.adapter.getService().getRoomManager().getRooms()) {
            AddGroupSvcPacket packet = new AddGroupSvcPacket();
            packet.setGroup(new SonusClientGroup(room, bypassPassword));
            connection.sendPacket(packet);
        }
    }

    public void tickKeepAlive() {
        broadcastPacket(new KeepAliveSvcPacket());
    }

    public void broadcastPacket(AbstractSvcPacket<?> packet) {
        for (SvcConnection conn : this.connections.values()) {
            if (conn.isConnected()) {
                conn.sendPacket(packet);
            }
        }
    }

    public void broadcastPacket(Function<SvcConnection, AbstractSvcPacket<?>> packet) {
        for (SvcConnection conn : this.connections.values()) {
            if (conn.isConnected()) {
                conn.sendPacket(packet.apply(conn));
            }
        }
    }

    public void broadcastPacketSourced(ISonusPlayer source, PlayerStateSvcPacket packet) {
        for (SvcConnection conn : this.connections.values()) {
            if (!conn.isConnected() || !conn.getPlayer().shouldSee(source)) {
                continue;
            }
            conn.getPlayer().ensureTabListed(source);
            conn.sendPacket(packet);
        }
    }

    public Map<UUID, SvcPlayerState> getPlayerStates(SvcConnection listener) {
        Collection<? extends ISonusPlayer> players = this.adapter.getService().getPlayerManager().getPlayers();
        Map<UUID, SvcPlayerState> states = new HashMap<>(this.connections.size());
        for (ISonusPlayer player : players) {
            if (!player.isConnected() || !player.shouldSee(listener.getPlayer())) {
                continue;
            }
            states.put(player.getUniqueId(), this.adapter.buildPlayerState(player));
        }
        return states;
    }

    public void removeSession(UUID playerId) {
        this.connections.remove(playerId);
    }

    public void broadcastNewGroup(IRoom room) {
        this.broadcastPacket(connection -> {
            boolean bypassPassword = connection.getPlayer().hasPermission(PERMISSION_BYPASS_GROUP_PASSWORD, TriState.NOT_SET);
            AddGroupSvcPacket packet = new AddGroupSvcPacket();
            SonusClientGroup group = new SonusClientGroup(room, bypassPassword);
            packet.setGroup(group);
            return packet;
        });
    }

    public void broadcastRemoveGroup(IRoom room) {
        RemoveGroupSvcPacket packet = new RemoveGroupSvcPacket();
        packet.setGroupId(room.getId());
        this.broadcastPacket(packet);
    }
}
