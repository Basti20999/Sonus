package dev.minceraft.sonus.protocol.meta;
// Created by booky10 in Sonus (01:56 17.07.2025)

import dev.minceraft.sonus.common.protocol.registry.SimpleRegistry;
import dev.minceraft.sonus.common.protocol.util.VarInt;
import dev.minceraft.sonus.protocol.meta.servicebound.BackendTickMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MetaRegistry {

    public static final SimpleRegistry<ByteBuf, IMetaMessage> REGISTRY =
            SimpleRegistry.Builder.<ByteBuf, IMetaMessage>createSimple()
                    .codec((buf, packet) -> packet.decode(buf), (buf, packet) -> packet.encode(buf))
                    .idCodec(VarInt::read, VarInt::write)
                    .register(BackendTickMessage.class, BackendTickMessage::new)
                    .build();

    private MetaRegistry() {
    }

    public static byte[] write(IMetaMessage message) {
        ByteBuf buf = Unpooled.buffer();
        try {
            REGISTRY.write(buf, message);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return data;
        } finally {
            buf.release();
        }
    }
}
