package dev.minceraft.sonus.protocol.registry;
// Created by booky10 in Sonus (01:46 17.07.2025)

import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ProtocolMessage<H> {

    void decode(ByteBuf buf, int version);

    void encode(ByteBuf buf, int version);

    void handle(H handler);
}
