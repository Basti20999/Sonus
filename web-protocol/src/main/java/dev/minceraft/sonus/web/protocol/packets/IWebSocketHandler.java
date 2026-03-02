package dev.minceraft.sonus.web.protocol.packets;

import dev.minceraft.sonus.web.protocol.packets.clientbound.CategoryAddPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.CategoryRemovePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.ConnectedPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomAddPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomJoinResponsePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomLeaveResponsePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomRemovePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RtcConnectPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.StateRemovePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.StateUpdatePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.VoiceActivityPacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.KeepAlivePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.PingPacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.RtcIceCandidatePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.RtcOfferPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomCreatePacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomJoinRequestPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomLeavePacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.StateInfoPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.VolumePacket;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IWebSocketHandler {

    default void handleCategoryAdd(CategoryAddPacket packet) {
    }

    default void handleCategoryRemove(CategoryRemovePacket packet) {
    }

    default void handleConnected(ConnectedPacket packet) {
    }

    default void handleRoomAdd(RoomAddPacket packet) {
    }

    default void handleRoomJoinResponse(RoomJoinResponsePacket packet) {
    }

    default void handleRoomLeaveResponse(RoomLeaveResponsePacket packet) {
    }

    default void handleRoomRemove(RoomRemovePacket packet) {
    }

    default void handleStateRemove(StateRemovePacket packet) {
    }

    default void handleStateUpdate(StateUpdatePacket packet) {
    }

    default void handleKeepAlive(KeepAlivePacket packet) {
    }

    default void handlePing(PingPacket packet) {
    }

    default void handleRoomCreate(RoomCreatePacket packet) {
    }

    default void handleRoomJoinRequest(RoomJoinRequestPacket packet) {
    }

    default void handleRoomLeave(RoomLeavePacket packet) {
    }

    default void handleStateInfo(StateInfoPacket packet) {
    }

    default void handleVoiceActivity(VoiceActivityPacket packet) {
    }

    default void handleRtcConnect(RtcConnectPacket packet) {
    }

    default void handleRtcIceCandidate(RtcIceCandidatePacket packet) {
    }

    default void handleRtcOffer(RtcOfferPacket packet) {
    }

    default void handleVolume(VolumePacket packet) {
    }
}
