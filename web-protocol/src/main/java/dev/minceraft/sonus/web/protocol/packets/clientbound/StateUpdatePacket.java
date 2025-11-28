package dev.minceraft.sonus.web.protocol.packets.clientbound;
// Created by booky10 in Sonus (20:35 28.11.2025)

import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.model.SonusWebPlayerState;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebsocketPacket;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class StateUpdatePacket extends WebsocketPacket {

    private @MonotonicNonNull SonusWebPlayerState state;

    public StateUpdatePacket(SonusWebPlayerState state) {
        this.state = state;
    }

    public StateUpdatePacket() {
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        SonusWebPlayerState.encode(buf, this.state);
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        this.failClientboundDecode();
        this.state = SonusWebPlayerState.decode(buf);
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handleStateUpdate(this);
    }

    public SonusWebPlayerState getState() {
        return this.state;
    }

    public void setState(SonusWebPlayerState state) {
        this.state = state;
    }
}
