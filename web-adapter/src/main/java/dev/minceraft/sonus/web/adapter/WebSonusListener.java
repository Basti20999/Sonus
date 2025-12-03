package dev.minceraft.sonus.web.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.service.ISonusServiceEvents;
import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.minceraft.sonus.web.protocol.model.SonusWebPlayerState;
import dev.minceraft.sonus.web.protocol.model.SonusWebRoom;
import dev.minceraft.sonus.web.protocol.packets.clientbound.PositionUpdatePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomAddPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomRemovePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.StateRemovePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.StateUpdatePacket;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_BYPASS_GROUP_PASSWORD;

@NullMarked
public class WebSonusListener implements ISonusServiceEvents {

    private final WebAdapter adapter;

    public WebSonusListener(WebAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onPlayerQuit(UUID playerId) {
        this.adapter.getSessions().removeSession(playerId);
        this.adapter.getSessions().broadcast(new StateRemovePacket(playerId));
    }

    @Override
    public void onPlayerStateUpdate(ISonusPlayer player) {
        this.adapter.getSessions().broadcastFrom(player, connection ->
                new StateUpdatePacket(SonusWebPlayerState.fromState(player, connection.getPlayer())));
    }

    @Override
    public void onPlayerPositionUpdate(ISonusPlayer player) {
        WebSocketConnection connection = this.adapter.getSessions().getConnection(player.getUniqueId());
        if (connection == null) {
            return; // No web connection
        }
        if (!connection.isConnected() || player.getPosition() == null) {
            return;
        }
        connection.sendPacket(new PositionUpdatePacket(player.getPosition()));
    }

    @Override
    public void onGroupCreate(IRoom room) {
        this.adapter.getSessions().broadcast(connection -> {
            boolean bypassPassword = connection.getPlayer().hasPermission(PERMISSION_BYPASS_GROUP_PASSWORD, false);
            return new RoomAddPacket(SonusWebRoom.fromRoom(room, bypassPassword));
        });
    }

    @Override
    public void onGroupRemove(IRoom room) {
        this.adapter.getSessions().broadcast(new RoomRemovePacket(room.getId()));
    }
}
