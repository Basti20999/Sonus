package dev.minecraft.sonus.common.protocol.codec;

import dev.minecraft.sonus.common.protocol.registry.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IBufCodec<C, H> extends ProtocolMessage<C, H> {

    void encode(ByteBuf buf);

    void decode(ByteBuf buf);
}
