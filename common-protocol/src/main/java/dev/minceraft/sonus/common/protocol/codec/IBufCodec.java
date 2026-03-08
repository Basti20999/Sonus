package dev.minceraft.sonus.common.protocol.codec;

import dev.minceraft.sonus.common.protocol.registry.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IBufCodec<H> extends ProtocolMessage<H> {

    void encode(ByteBuf buf);

    void decode(ByteBuf buf);
}
