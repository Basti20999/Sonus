package dev.minceraft.sonus.web.pion;
// Created by booky10 in Sonus (4:59 PM 06.03.2026)

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PionRemoteTrack implements AutoCloseable {

    public int getSampleRate() {

    }

    public short getChannels() {

    }

    @Override
    public void close() {
    }

    public interface Callback {

        void handleData(byte[] data, long durationNanos);
    }
}
