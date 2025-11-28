package dev.minceraft.sonus.web.protocol.packets.commonbound;
// Created by booky10 in Sonus (20:31 28.11.2025)

import dev.minceraft.sonus.common.protocol.util.VarLong;
import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebsocketPacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PingPacket extends WebsocketPacket {

    private long id;

    public PingPacket(long id) {
        this.id = id;
    }

    public PingPacket() {
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        VarLong.write(buf, this.id);
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        this.id = VarLong.read(buf);
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handlePing(this);
    }
}
