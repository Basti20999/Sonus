package dev.minceraft.sonus.protocol.meta;
// Created by booky10 in Sonus (01:11 17.07.2025)

import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IMetaMessage {

    void decode(ByteBuf buf, int version);

    void encode(ByteBuf buf, int version);

    void handle(IMetaHandler handler);
}
