package dev.minceraft.sonus.web.protocol.packets.servicebound;
// Created by booky10 in Sonus (20:35 28.11.2025)

import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebSocketPacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class StateInfoPacket extends WebSocketPacket {

    private boolean muted;
    private boolean deafened;

    public StateInfoPacket(boolean muted, boolean deafened) {
        this.muted = muted;
        this.deafened = deafened;
    }

    public StateInfoPacket() {
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        buf.writeBoolean(this.muted);
        buf.writeBoolean(this.deafened);
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        this.muted = buf.readBoolean();
        this.deafened = buf.readBoolean();
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handleStateInfo(this);
    }

    public boolean isMuted() {
        return this.muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isDeafened() {
        return this.deafened;
    }

    public void setDeafened(boolean deafened) {
        this.deafened = deafened;
    }
}
