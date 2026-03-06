package dev.minceraft.sonus.web.pion.ipc;
// Created by booky10 in Sonus (7:21 PM 06.03.2026)

import org.jspecify.annotations.NullMarked;

@NullMarked
public class IpcError extends Error {

    public IpcError(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
