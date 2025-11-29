package dev.minceraft.sonus.web.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.minceraft.sonus.web.adapter.util.WebTokenUtil;
import dev.minceraft.sonus.web.protocol.AbstractWebPacket;
import dev.minceraft.sonus.web.protocol.model.SonusWebPlayerState;
import dev.minceraft.sonus.web.protocol.model.SonusWebRoom;
import dev.minceraft.sonus.web.protocol.packets.clientbound.ConnectedPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomAddPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.StateUpdatePacket;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_BYPASS_GROUP_PASSWORD;

public class WebSessionManager {

    private final WebAdapter adapter;
    private final Map<String, WeakReference<ISonusPlayer>> tokens = new ConcurrentHashMap<>();
    private final Map<UUID, WebSocketConnection> connections = new ConcurrentHashMap<>();

    public WebSessionManager(WebAdapter adapter) {
        this.adapter = adapter;
    }

    public void onConnectionEstablished(WebSocketConnection connection) {
        // send group initialization packets for everything
        boolean bypassPassword = connection.getPlayer().hasPermission(PERMISSION_BYPASS_GROUP_PASSWORD, false);
        for (IRoom room : this.adapter.getService().getRoomManager().getRooms()) {
            RoomAddPacket packet = new RoomAddPacket();
            packet.setRoom(SonusWebRoom.fromRoom(room, bypassPassword));
            connection.sendPacket(packet);
        }

        // broadcast new player state to everyone else
        connection.getPlayer().updateState();

        // initialize all player states
        for (ISonusPlayer target : this.adapter.getService().getPlayerManager().getPlayers()) {
            // build state update of target if player can see target
            if (target.isConnected() && connection.getPlayer().canSee(target)) {
                StateUpdatePacket packet = new StateUpdatePacket();
                packet.setState(SonusWebPlayerState.fromState(target, connection.getPlayer()));
                connection.sendPacket(packet);
            }
        }

        // inform the player that they are fully connected
        UUID playerId = connection.getPlayer().getUniqueId();
        String username = connection.getPlayer().getName();
        connection.sendPacket(new ConnectedPacket(playerId, Component.text(username)));
    }

    public void broadcast(AbstractWebPacket<?> packet) {
        this.broadcast(__ -> packet);
    }

    public void broadcast(Function<WebSocketConnection, AbstractWebPacket<?>> packet) {
        for (WebSocketConnection conn : this.connections.values()) {
            if (conn.isConnected()) {
                conn.sendPacket(packet.apply(conn));
            }
        }
    }

    public void broadcastFrom(ISonusPlayer source, AbstractWebPacket<?> packet) {
        this.broadcastFrom(source, __ -> packet);
    }

    public void broadcastFrom(ISonusPlayer source, Function<WebSocketConnection, AbstractWebPacket<?>> packet) {
        for (WebSocketConnection conn : this.connections.values()) {
            if (!conn.isConnected() || !conn.getPlayer().canSee(source)) {
                continue; // target not connected or target can't see source
            }
            conn.getPlayer().ensureTabListed(source);
            conn.sendPacket(packet.apply(conn));
        }
    }

    public String generateToken(ISonusPlayer player) {
        String token = WebTokenUtil.generateToken();
        this.tokens.put(token, new WeakReference<>(player));
        return token;
    }

    public @Nullable ISonusPlayer getByToken(String token) {
        WeakReference<ISonusPlayer> player = this.tokens.get(token);
        return player != null ? player.get() : null;
    }

    public void addConnection(WebSocketConnection connection) {
        this.connections.put(connection.getPlayer().getUniqueId(), connection);
    }

    public @Nullable WebSocketConnection getConnection(UUID playerId) {
        return this.connections.get(playerId);
    }

    public void removeSession(UUID playerId) {
        try (WebSocketConnection ignoredConn = this.connections.remove(playerId)) {
            // NO-OP
        }
    }
}
