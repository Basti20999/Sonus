package dev.minceraft.sonus.web.protocol.packets.servicebound;
// Created by booky10 in Sonus (02:50 23.12.2025)

import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebSocketPacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class InputEndPacket extends WebSocketPacket {

    public static final InputEndPacket INSTANCE = new InputEndPacket();

    private InputEndPacket() {
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        // NO-OP
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        // NO-OP
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handleInputEnd(this);
    }
}
