package dev.minceraft.sonus.svc.protocol;
// Created by booky10 in Sonus (21:42 24.11.2025)

import org.jspecify.annotations.NullMarked;

@NullMarked
public record SvcPacketContext(int version) {

    public static final SvcPacketContext INITIAL = new SvcPacketContext(-1);

    public SvcPacketContext withVersion(int version) {
        return new SvcPacketContext(version);
    }
}
