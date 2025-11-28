package dev.minceraft.sonus.web.protocol.packets.clientbound;
// Created by booky10 in Sonus (20:34 28.11.2025)

import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.model.SonusWebRoom;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebsocketPacket;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RoomAddPacket extends WebsocketPacket {

    private @MonotonicNonNull SonusWebRoom room;

    public RoomAddPacket(SonusWebRoom room) {
        this.room = room;
    }

    public RoomAddPacket() {
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        SonusWebRoom.encode(buf, this.room);
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        this.failClientboundDecode();
        this.room = SonusWebRoom.decode(buf);
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handleRoomAdd(this);
    }

    public SonusWebRoom getRoom() {
        return this.room;
    }

    public void setRoom(SonusWebRoom room) {
        this.room = room;
    }
}
