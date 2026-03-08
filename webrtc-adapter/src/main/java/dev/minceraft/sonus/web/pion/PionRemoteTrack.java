package dev.minceraft.sonus.web.pion;
// Created by booky10 in Sonus (4:59 PM 06.03.2026)

import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PionRemoteTrack {

    final int trackId;
    private final int sampleRate;
    private final short channels;

    PionRemoteTrack(int trackId, int sampleRate, short channels) {
        this.trackId = trackId;
        this.sampleRate = sampleRate;
        this.channels = channels;
    }

    public int getSampleRate() {
        return this.sampleRate;
    }

    public short getChannels() {
        return this.channels;
    }

    public interface Callback {

        void onData(ByteBuf data, long durationNanos);
    }
}
