package dev.minceraft.sonus.web.protocol;
// Created by booky10 in Sonus (21:42 24.11.2025)

import org.jspecify.annotations.NullMarked;

@NullMarked
public record WsPacketContext(int version) {

    public static final WsPacketContext INITIAL = new WsPacketContext(-1);

    public WsPacketContext withVersion(int version) {
        return new WsPacketContext(version);
    }
}
