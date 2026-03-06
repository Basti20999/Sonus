package dev.minceraft.sonus.web.adapter.connection;

import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.service.ISonusRoomManager;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomJoinResponsePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomLeaveResponsePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.KeepAlivePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.PingPacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.RtcIceCandidatePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.RtcOfferPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomCreatePacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomJoinRequestPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomLeavePacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.StateInfoPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.VolumePacket;
import net.kyori.adventure.util.Index;
import org.freedesktop.gstreamer.webrtc.WebRTCSDPType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public class WebSocketPacketHandler implements IWebSocketHandler {

    private final WebSocketConnection connection;
    private State state = State.WAITING_ACK;

    public WebSocketPacketHandler(WebSocketConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleKeepAlive(KeepAlivePacket packet) {
        if (this.state == State.CONNECTED) {
            this.connection.getPlayer().setKeepAlive(System.currentTimeMillis());
        }
    }

    @Override
    public void handlePing(PingPacket packet) {
        if (this.state == State.CONNECTED) {
            this.connection.sendPacket(packet);
        }
    }

    private void tryJoinRoom(UUID roomId, @Nullable String password) {
        IRoom room = this.connection.getAdapter().getService().getRoomManager().getRoom(roomId);
        boolean success = room != null && this.connection.getPlayer().canAccessRoom(room, password);
        if (success) {
            // if player can access room, update primary room
            this.connection.getPlayer().setPrimaryRoom(room);
        }
        this.connection.sendPacket(new RoomJoinResponsePacket(roomId, success));
    }

    @Override
    public void handleRoomCreate(RoomCreatePacket packet) {
        if (this.state != State.CONNECTED) {
            return;
        }
        if (!this.connection.getPlayer().hasPermission(SonusConstants.PERMISSION_GROUPS_USE, true)) {
            this.connection.sendPacket(new RoomJoinResponsePacket(UUID.randomUUID(), false));
            return;
        }
        ISonusRoomManager rooms = this.connection.getAdapter().getService().getRoomManager();
        IRoom room = rooms.createStaticRoom(packet.getName(), packet.getPassword(), packet.getAudioType(), false);
        this.tryJoinRoom(room.getId(), packet.getPassword());
    }

    @Override
    public void handleRoomJoinRequest(RoomJoinRequestPacket packet) {
        if (this.state == State.CONNECTED) {
            this.tryJoinRoom(packet.getRoomId(), packet.getPassword());
        }
    }

    @Override
    public void handleRoomLeave(RoomLeavePacket packet) {
        if (this.state != State.CONNECTED) {
            return;
        }
        ISonusPlayer player = this.connection.getPlayer();
        IRoom primaryRoom = player.getPrimaryRoom();
        // prevent accidentally leaving wrong room because of desyncs
        boolean success;
        if (primaryRoom != null && primaryRoom.getId().equals(packet.getRoomId())) {
            player.setPrimaryRoom(null);
            success = true;
        } else {
            success = false;
        }
        this.connection.sendPacket(new RoomLeaveResponsePacket(packet.getRoomId(), success));
    }

    @Override
    public void handleStateInfo(StateInfoPacket packet) {
        ISonusPlayer player = this.connection.getPlayer();
        if (this.state == State.WAITING_ACK) {
            player.handleConnect();
            this.state = State.CONNECTED;
        }
        player.setMuted(packet.isMuted());
        player.setDeafened(packet.isDeafened());
        player.updateState();
    }

    @Override
    public void handleRtcIceCandidate(RtcIceCandidatePacket packet) {
        this.connection.getRtc().handleRemoteIce(packet.getSdp(), packet.getSdpMLineIndex());
    }

    private static final Index<String, WebRTCSDPType> RTC_SDP_TYPE_INDEX = Index.create(WebRTCSDPType.class,
            type -> type.name().toLowerCase());

    @Override
    public void handleRtcOffer(RtcOfferPacket packet) {
        WebRTCSDPType type = RTC_SDP_TYPE_INDEX.valueOrThrow(packet.getType());
        if (type != WebRTCSDPType.OFFER || packet.getSdp() == null) {
            throw new IllegalStateException("Unexpected packet: " + packet);
        }
        this.connection.getRtc().handleRemoteOffer(type, packet.getSdp());
    }

    @Override
    public void handleVolume(VolumePacket packet) {
        this.connection.setVolume(packet.getType(), packet.getEntryId(), packet.getVolume());
    }

    public void handleDisconnect() {
        this.state = State.DISCONNECTED;
        this.connection.getPlayer().disconnect();
    }

    public enum State {
        WAITING_ACK,
        CONNECTED,
        DISCONNECTED,
    }
}
