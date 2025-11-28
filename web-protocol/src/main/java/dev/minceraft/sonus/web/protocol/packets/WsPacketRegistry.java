package dev.minceraft.sonus.web.protocol.packets;

import dev.minceraft.sonus.common.protocol.registry.ContextedRegistry;
import dev.minceraft.sonus.common.protocol.util.VarInt;
import dev.minceraft.sonus.web.protocol.WsPacketContext;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WsPacketRegistry {

    private WsPacketRegistry() {
    }

    public static final ContextedRegistry<ByteBuf, WebsocketPacket<?>, WsPacketContext> REGISTRY =
            ContextedRegistry.Builder.<ByteBuf, WebsocketPacket<?>, WsPacketContext>createContext()
                    .codec((buf, packet, ctx) -> packet.decode(buf, ctx),
                            (buf, packet, ctx) -> packet.encode(buf, ctx))
                    .idCodec((buf, __) -> VarInt.read(buf),
                            (buf, id, __) -> VarInt.write(buf, id))
                    .build();
}
