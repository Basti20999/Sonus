package dev.minceraft.sonus.protocol.meta;
// Created by booky10 in Sonus (01:11 17.07.2025)

import dev.minceraft.sonus.common.protocol.codec.IBufCodec;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IMetaMessage extends IBufCodec<IMetaHandler> {
}
