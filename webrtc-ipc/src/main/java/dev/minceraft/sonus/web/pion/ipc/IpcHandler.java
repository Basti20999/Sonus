package dev.minceraft.sonus.web.pion.ipc;
// Created by booky10 in Sonus (6:54 PM 06.03.2026)

import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface IpcHandler {

    void handle(IpcMessage message);
}
