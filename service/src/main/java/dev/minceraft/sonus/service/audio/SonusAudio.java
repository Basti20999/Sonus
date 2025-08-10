package dev.minceraft.sonus.service.audio;
// Created by booky10 in Sonus (02:44 17.07.2025)

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SonusAudio {

    private final byte[] data;

    public SonusAudio(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }
}
