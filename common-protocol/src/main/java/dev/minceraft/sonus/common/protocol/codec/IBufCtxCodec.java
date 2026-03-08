package dev.minceraft.sonus.common.protocol.codec;
// Created by booky10 in Sonus (21:41 24.11.2025)

import dev.minceraft.sonus.common.protocol.registry.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IBufCtxCodec<H, C> extends ProtocolMessage<H> {

    void encode(ByteBuf buf, C context);

    void decode(ByteBuf buf, C context);
}
