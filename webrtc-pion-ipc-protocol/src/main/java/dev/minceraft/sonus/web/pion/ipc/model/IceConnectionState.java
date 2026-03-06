package dev.minceraft.sonus.web.pion.ipc.model;
// Created by booky10 in Sonus (5:16 PM 06.03.2026)

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum IceConnectionState {

    UNKNOWN,
    NEW,
    CHECKING,
    CONNECTED,
    COMPLETED,
    DISCONNECTED,
    FAILED,
    CLOSED,
}
