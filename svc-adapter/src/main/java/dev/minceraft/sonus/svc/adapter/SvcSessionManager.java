package dev.minceraft.sonus.svc.adapter;

import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import dev.minceraft.sonus.svc.protocol.AbstractSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStateSvcPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SvcSessionManager {

    private final SvcAdapter adapter;
    private final Map<UUID, SvcConnection> connection = new HashMap<>();

    public SvcSessionManager(SvcAdapter adapter) {
        this.adapter = adapter;
    }

    public SvcConnection getConnection(UUID playerId) {
        return this.connection.get(playerId);
    }

    public void addConnection(SvcConnection connection) {
        this.connection.put(connection.getPlayer().getUniqueId(), connection);
    }

    public void onConnectionEstablished(SvcConnection connection) {
        PlayerStateSvcPacket packet = new PlayerStateSvcPacket();
        packet.setState(connection.buildState());

        this.broadcastPacket(packet);
    }

    public void broadcastPacket(AbstractSvcPacket<?> packet) {
        for (SvcConnection conn : this.connection.values()) {
            conn.sendPacket(packet);
        }
    }

    public void removeSession(UUID playerId) {
        this.connection.remove(playerId);
    }
}
