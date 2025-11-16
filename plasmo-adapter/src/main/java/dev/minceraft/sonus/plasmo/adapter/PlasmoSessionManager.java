package dev.minceraft.sonus.plasmo.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.plasmo.adapter.connection.PlasmoConnection;
import dev.minceraft.sonus.plasmo.protocol.AbstractPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.udp.bothbound.PingPlasmoPacket;
import net.kyori.adventure.util.TriState;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_CONNECT_PLASMO;

@NullMarked
public class PlasmoSessionManager {

    private final PlasmoAdapter adapter;
    private final Map<UUID, PlasmoConnection> usersByUniqueId = new HashMap<>();
    private final Map<UUID, PlasmoConnection> usersBySecret = new HashMap<>();

    public PlasmoSessionManager(PlasmoAdapter adapter) {
        this.adapter = adapter;

        this.adapter.getService().getScheduler().schedule(this::tickKeepAlive,
                0,
                this.adapter.getService().getConfig().getKeepAliveMs(),
                TimeUnit.MILLISECONDS);
    }

    public void tickKeepAlive() {
        PingPlasmoPacket packet = new PingPlasmoPacket();
        packet.setTime(System.currentTimeMillis());
        broadcastPacket(packet);
    }

    public void broadcastPacket(AbstractPlasmoPacket<?> packet) {
        for (PlasmoConnection conn : this.usersByUniqueId.values()) {
            if (!conn.isConnected()) {
                continue;
            }
            conn.sendPacket(packet);
        }
    }

    public void broadcastState(PlasmoConnection connection) {

    }

    @Nullable
    public PlasmoConnection getConnectionBySecret(UUID secret) {
        synchronized (this) {
            return this.usersBySecret.get(secret);
        }
    }

    @Nullable
    public PlasmoConnection getConnectionByUniqueId(UUID uniqueId) {
        synchronized (this) {
            return this.usersByUniqueId.get(uniqueId);
        }
    }

    @Nullable
    public PlasmoConnection createConnection(UUID playerId) {
        ISonusPlayer player = this.adapter.getService().getPlayerManager().getPlayer(playerId);
        if (player == null) {
            return null;
        }
        if (!player.hasPermission(PERMISSION_CONNECT_PLASMO, true)) {
            return null;
        }
        PlasmoConnection plasmoConnection = new PlasmoConnection(this.adapter, player);
        synchronized (this) {
            this.usersBySecret.put(plasmoConnection.getSecret(), plasmoConnection);
            this.usersByUniqueId.put(playerId, plasmoConnection);
        }
        return plasmoConnection;
    }
}
