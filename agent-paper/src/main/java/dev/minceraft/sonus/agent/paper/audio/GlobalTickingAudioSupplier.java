package dev.minceraft.sonus.agent.paper.audio;
// Created by booky10 in Sonus (00:50 24.11.2025)

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

import static dev.minceraft.sonus.common.SonusConstants.FRAME_SIZE;
import static dev.minceraft.sonus.common.SonusConstants.SAMPLE_RATE;

@NullMarked
public final class GlobalTickingAudioSupplier implements AudioSupplier {

    private static final long FRAME_INTERVAL_NANOS = 1000 / (SAMPLE_RATE / FRAME_SIZE) * 1_000_000L;

    private final AudioSupplier delegate;
    private volatile long nextTick = System.nanoTime(); // nanoTime when next tick should occur
    private volatile short @Nullable [] samples;

    public GlobalTickingAudioSupplier(AudioSupplier delegate) {
        this.delegate = delegate;
    }

    private synchronized void tick() {
        short[] samples = this.delegate.get();
        if (samples != null && samples.length != FRAME_SIZE) {
            throw new IllegalStateException(this.delegate + " has provided unexpected PCM data length: " + Arrays.toString(samples));
        }
        this.samples = samples;
    }

    public short @Nullable [] get() {
        long now = System.nanoTime();
        if (now >= this.nextTick) {
            synchronized (this) {
                if (now >= this.nextTick) {
                    this.nextTick += FRAME_INTERVAL_NANOS;
                    this.tick();
                }
            }
        }
        return this.samples;
    }

    @ApiStatus.Internal
    public void waitNextTick() {
        long wait = this.nextTick - System.nanoTime();
        if (wait > 0L) {
            LockSupport.parkNanos(wait);
        }
    }
}
