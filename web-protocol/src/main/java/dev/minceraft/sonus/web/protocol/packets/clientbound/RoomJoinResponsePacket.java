package dev.minceraft.sonus.web.protocol.packets.clientbound;
// Created by booky10 in Sonus (20:34 28.11.2025)

import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebsocketPacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RoomJoinResponsePacket extends WebsocketPacket {

    private boolean success;

    public RoomJoinResponsePacket(boolean success) {
        this.success = success;
    }

    public RoomJoinResponsePacket() {
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        buf.writeBoolean(this.success);
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        this.success = buf.readBoolean();
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handleRoomJoinResponse(this);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
