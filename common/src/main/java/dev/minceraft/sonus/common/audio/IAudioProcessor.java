package dev.minceraft.sonus.common.audio;
// Created by booky10 in Sonus (04:38 16.11.2025)

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IAudioProcessor extends AutoCloseable {

    short[] decode(byte[] data);

    byte[] encode(short[] pcm);

    @Override
    void close();
}
