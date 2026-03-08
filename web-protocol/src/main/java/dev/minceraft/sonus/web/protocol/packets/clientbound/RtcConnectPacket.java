package dev.minceraft.sonus.web.protocol.packets.clientbound;
// Created by booky10 in Sonus (5:23 PM 02.03.2026)

import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebSocketPacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RtcConnectPacket extends WebSocketPacket {

    public static final RtcConnectPacket INSTANCE = new RtcConnectPacket();

    private RtcConnectPacket() {
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
        handler.handleRtcConnect(this);
    }
}
