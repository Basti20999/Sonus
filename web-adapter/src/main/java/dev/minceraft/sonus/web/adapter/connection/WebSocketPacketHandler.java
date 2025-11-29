package dev.minceraft.sonus.web.adapter.connection;

import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.service.ISonusRoomManager;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomJoinResponsePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.KeepAlivePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.PingPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.InputSoundPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomCreatePacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomJoinRequestPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomLeavePacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.StateInfoPacket;

import java.util.UUID;

public class WebSocketPacketHandler implements IWebSocketHandler {

    private static final UUID MIC_CHANNEL_ID = new UUID(9018035903106730674L, -6405133132459802568L);

    private final WebSocketConnection connection;

    public WebSocketPacketHandler(WebSocketConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleKeepAlive(KeepAlivePacket packet) {
        this.connection.getPlayer().setKeepAlive(System.currentTimeMillis());
    }

    @Override
    public void handlePing(PingPacket packet) {
        this.connection.sendPacket(packet);
    }

    @Override
    public void handleInputSound(InputSoundPacket packet) {
        SonusAudio.Pcm pcm = packet.getAudio().asPcm(() -> this.connection.getProcessor(MIC_CHANNEL_ID));
        this.connection.getPlayer().handleAudioInput(pcm);
    }

    private void tryJoinRoom(UUID roomId, String password) {
        IRoom room = this.connection.getAdapter().getService().getRoomManager().getRoom(roomId);
        boolean success = room != null && this.connection.getPlayer().canAccessRoom(room, password);
        if (success) {
            // if player can access room, update primary room
            this.connection.getPlayer().setPrimaryRoom(room);
        }
        this.connection.sendPacket(new RoomJoinResponsePacket(success));
    }

    @Override
    public void handleRoomCreate(RoomCreatePacket packet) {
        ISonusRoomManager rooms = this.connection.getAdapter().getService().getRoomManager();
        IRoom room = rooms.createStaticRoom(packet.getName(), packet.getPassword(), packet.getAudioType(), false);
        this.tryJoinRoom(room.getId(), packet.getPassword());
    }

    @Override
    public void handleRoomJoinRequest(RoomJoinRequestPacket packet) {
        this.tryJoinRoom(packet.getRoomId(), packet.getPassword());
    }

    @Override
    public void handleRoomLeave(RoomLeavePacket packet) {
        this.connection.getPlayer().setPrimaryRoom(null);
    }

    @Override
    public void handleStateInfo(StateInfoPacket packet) {
        ISonusPlayer player = this.connection.getPlayer();
        player.setMuted(packet.isMuted());
        player.setDeafened(packet.isDeafened());
    }
}
