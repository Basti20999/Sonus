package dev.minceraft.sonus.common.protocol.registry;
// Created by booky10 in Sonus (01:46 17.07.2025)

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ProtocolMessage<H> {

    void handle(H handler);
}
