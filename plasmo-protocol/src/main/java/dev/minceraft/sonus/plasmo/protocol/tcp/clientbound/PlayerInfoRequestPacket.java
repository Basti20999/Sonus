package dev.minceraft.sonus.plasmo.protocol.tcp.clientbound;

import dev.minceraft.sonus.plasmo.protocol.tcp.TcpHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPlasmoPacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerInfoRequestPacket extends TcpPlasmoPacket<PlayerInfoRequestPacket> {

    public PlayerInfoRequestPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {

    }

    @Override
    public void decode(ByteBuf buf) {

    }

    @Override
    public void handle(TcpHandler handler) {
        handler.handlePlayerInfoRequestPacket(this);
    }
}
