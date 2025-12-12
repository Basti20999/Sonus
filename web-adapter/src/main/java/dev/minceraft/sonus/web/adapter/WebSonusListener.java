package dev.minceraft.sonus.web.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.WorldRotatedVec3d;
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

@NullMarked
public class WebSonusListener implements ISonusServiceEvents {

    private final WebAdapter adapter;

    public WebSonusListener(WebAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onPlayerSwitchBackend(UUID playerId) {
        WebSocketConnection connection = this.adapter.getSessions().getConnection(playerId);
        if (connection != null) {
            connection.sendConnected();
        }
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
        if (!player.isConnected()) {
            return;
        }
        WebSocketConnection connection = this.adapter.getSessions().getConnection(player.getUniqueId());
        if (connection == null) {
            return;
        }
        WorldRotatedVec3d position = player.getPosition();
        if (position != null) {
            connection.sendPacket(new PositionUpdatePacket(position));
        }
    }

    @Override
    public void onGroupCreate(IRoom room) {
        this.adapter.getSessions().broadcast(connection ->
                new RoomAddPacket(SonusWebRoom.fromRoom(room, connection.getPlayer())));
    }

    @Override
    public void onGroupRemove(IRoom room) {
        this.adapter.getSessions().broadcast(new RoomRemovePacket(room.getId()));
    }
}
