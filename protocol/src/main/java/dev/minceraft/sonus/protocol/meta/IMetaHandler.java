package dev.minceraft.sonus.protocol.meta;
// Created by booky10 in Sonus (01:14 17.07.2025)

import dev.minceraft.sonus.protocol.meta.servicebound.BackendTickMessage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IMetaHandler {

    void handle(BackendTickMessage message);
}
