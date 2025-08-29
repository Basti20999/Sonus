package dev.minceraft.sonus.svc.adapter;

import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import dev.minceraft.sonus.svc.protocol.AbstractSvcPacket;
import dev.minceraft.sonus.svc.protocol.data.SonusClientGroup;
import dev.minceraft.sonus.svc.protocol.data.SonusPlayerState;
import dev.minceraft.sonus.svc.protocol.meta.AddGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStateSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStatesSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemoveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.KeepAliveSvcPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        this.broadcastState(connection);

        PlayerStatesSvcPacket statesPacket = new PlayerStatesSvcPacket();
        statesPacket.setStates(this.getPlayerStates());
        connection.sendPacket(statesPacket);
    }

    public void tickKeepAlive() {
        broadcastPacket(new KeepAliveSvcPacket());
    }

    public void broadcastPacket(AbstractSvcPacket<?> packet) {
        for (SvcConnection conn : this.connections.values()) {
            if (!conn.isConnected()) {
                continue;
            }
            conn.sendPacket(packet);
        }
    }

    public Map<UUID, SonusPlayerState> getPlayerStates() {
        Map<UUID, SonusPlayerState> states = new HashMap<>(this.connections.size());
        for (SvcConnection conn : this.connections.values()) {
            states.put(conn.getPlayer().getUniqueId(), conn.buildState());
        }
        return states;
    }

    public void removeSession(UUID playerId) {
        this.connections.remove(playerId);
    }

    public void broadcastState(SvcConnection connection) {
        PlayerStateSvcPacket statePacket = new PlayerStateSvcPacket();
        statePacket.setState(connection.buildState());
        this.broadcastPacket(statePacket);
    }

    public void broadcastNewGroup(IRoom room) {
        AddGroupSvcPacket packet = new AddGroupSvcPacket();
        SonusClientGroup group = new SonusClientGroup(room);
        packet.setGroup(group);
        this.broadcastPacket(packet);
    }

    public void broadcastRemoveGroup(IRoom room) {
        RemoveGroupSvcPacket packet = new RemoveGroupSvcPacket();
        packet.setGroupId(room.getId());
        this.broadcastPacket(packet);
    }
}
