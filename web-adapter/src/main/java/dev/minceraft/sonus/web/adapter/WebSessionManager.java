package dev.minceraft.sonus.web.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.minceraft.sonus.web.adapter.util.WebTokenUtil;
import dev.minceraft.sonus.web.protocol.AbstractWebPacket;
import dev.minceraft.sonus.web.protocol.model.SonusWebPlayerState;
import dev.minceraft.sonus.web.protocol.model.SonusWebRoom;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomAddPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.StateUpdatePacket;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class WebSessionManager {

    private final WebAdapter adapter;
    private final Map<String, UUID> tokens = new ConcurrentHashMap<>();
    private final Map<UUID, WebSocketConnection> connections = new ConcurrentHashMap<>();

    public WebSessionManager(WebAdapter adapter) {
        this.adapter = adapter;
    }

    public void onConnectionEstablished(WebSocketConnection connection) {
        // send group initialization packets for everything
        ISonusPlayer player = connection.getPlayer();
        for (IRoom room : this.adapter.getService().getRoomManager().getRooms()) {
            RoomAddPacket packet = new RoomAddPacket();
            packet.setRoom(SonusWebRoom.fromRoom(room, player));
            connection.sendPacket(packet);
        }

        // broadcast new player state to everyone else
        player.updateState();

        // initialize all player states
        for (ISonusPlayer target : this.adapter.getService().getPlayerManager().getPlayers()) {
            // build state update of target if player can see target
            if (target.isConnected() && player.canSee(target)) {
                StateUpdatePacket packet = new StateUpdatePacket();
                packet.setState(SonusWebPlayerState.fromState(target, player));
                connection.sendPacket(packet);
            }
        }

        // inform the player that they are fully connected
        connection.sendConnected();
    }

    public void broadcast(AbstractWebPacket<?> packet) {
        this.broadcast(__ -> packet);
    }

    public void broadcast(Function<WebSocketConnection, AbstractWebPacket<?>> packet) {
        for (WebSocketConnection conn : this.connections.values()) {
            if (conn.getPlayer().isConnected()) {
                conn.sendPacket(packet.apply(conn));
            }
        }
    }

    public void broadcastFrom(ISonusPlayer source, AbstractWebPacket<?> packet) {
        this.broadcastFrom(source, __ -> packet);
    }

    public void broadcastFrom(ISonusPlayer source, Function<WebSocketConnection, AbstractWebPacket<?>> packet) {
        for (WebSocketConnection conn : this.connections.values()) {
            ISonusPlayer target = conn.getPlayer();
            // ensure target is connected and target can see source
            if (target.isConnected() && target.canSee(source)) {
                conn.sendPacket(packet.apply(conn));
            }
        }
    }

    public String generateToken(ISonusPlayer player) {
        String token = WebTokenUtil.generateToken();
        this.tokens.put(token, player.getUniqueId());
        return token;
    }

    public void removeTokens(UUID playerId) {
        this.tokens.values().removeIf(Predicate.isEqual(playerId));
    }

    public @Nullable ISonusPlayer consumeToken(String token) {
        UUID playerId = this.tokens.remove(token);
        if (playerId != null) {
            this.removeTokens(playerId);
        }

        ISonusPlayer player = this.adapter.getService().getPlayerManager().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            return player;
        }
        return null;
    }

    public void addConnection(WebSocketConnection connection) {
        this.connections.put(connection.getPlayer().getUniqueId(), connection);
    }

    public @Nullable WebSocketConnection getConnection(UUID playerId) {
        return this.connections.get(playerId);
    }

    public boolean removeSession(UUID playerId) {
        try (WebSocketConnection conn = this.connections.remove(playerId)) {
            return conn != null;
        }
    }
}
