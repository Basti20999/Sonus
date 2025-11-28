package dev.minceraft.sonus.web.protocol.packets.clientbound;
// Created by booky10 in Sonus (20:32 28.11.2025)

import dev.minceraft.sonus.common.data.Vec3d;
import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebsocketPacket;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PositionUpdatePacket extends WebsocketPacket {

    private @MonotonicNonNull Vec3d position;

    public PositionUpdatePacket(Vec3d position) {
        this.position = position;
    }

    public PositionUpdatePacket() {
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        Vec3d.encode(buf, this.position);
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        this.position = Vec3d.decode(buf);
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handlePositionUpdate(this);
    }

    public Vec3d getPosition() {
        return this.position;
    }

    public void setPosition(Vec3d position) {
        this.position = position;
    }
}
