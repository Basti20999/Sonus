package dev.minceraft.sonus.plasmo.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.plasmo.adapter.connection.PlasmoConnection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NullMarked
public class PlasmoSessionManager {

    private final PlasmoAdapter adapter;
    private final Map<UUID, PlasmoConnection> usersBySecret = new HashMap<>();

    public PlasmoSessionManager(PlasmoAdapter adapter) {
        this.adapter = adapter;
    }

    public PlasmoConnection getConnection(UUID secret) {
        return this.usersBySecret.get(secret);
    }

    @Nullable
    public PlasmoConnection createConnection(UUID playerId) {
        ISonusPlayer player = this.adapter.getService().getPlayerManager().getPlayer(playerId);
        if (player == null) {
            return null;
        }
        PlasmoConnection plasmoConnection = new PlasmoConnection(this.adapter, player);
        this.usersBySecret.put(plasmoConnection.getSecret(), plasmoConnection);
        return plasmoConnection;
    }
}
