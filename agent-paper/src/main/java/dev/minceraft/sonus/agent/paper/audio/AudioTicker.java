package dev.minceraft.sonus.agent.paper.audio;
// Created by booky10 in Sonus (00:50 24.11.2025)

import com.google.common.collect.ImmutableList;
import dev.minceraft.sonus.common.audio.AudioProcessor;
import dev.minceraft.sonus.protocol.meta.servicebound.AudioStreamMessage.Frame;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import static dev.minceraft.sonus.common.SonusConstants.FRAME_SIZE;
import static dev.minceraft.sonus.common.SonusConstants.SAMPLE_RATE;

@NullMarked
public final class AudioTicker implements AutoCloseable {

    private static final long FRAME_INTERVAL_NANOS = 1000 / (SAMPLE_RATE / FRAME_SIZE) * 1_000_000L;

    private final AudioSupplier delegate;
    private final AudioProcessor processor;
    private final int frameCount; // frames to tick at once
    private volatile long nextTick = System.nanoTime(); // nanoTime when next tick should occur

    private List<Frame> frames = List.of();
    private long sequenceNumber = 0L;

    public AudioTicker(AudioSupplier delegate, AudioProcessor processor, int frameCount) {
        this.delegate = delegate;
        this.processor = processor;
        this.frameCount = frameCount;
    }

    private synchronized void tick() {
        ImmutableList.Builder<Frame> frames = ImmutableList.builderWithExpectedSize(this.frameCount);
        for (int i = 0; i < this.frameCount; i++) {
            short[] pcm = this.delegate.getAndTick();
            if (pcm == null) {
                break; // end of stream
            } else if (pcm.length != FRAME_SIZE) {
                throw new IllegalStateException(this.delegate + " has provided unexpected PCM data length: " + Arrays.toString(pcm));
            }
            byte[] opus = this.processor.encode(pcm);
            long seq = this.sequenceNumber++;
            frames.add(new Frame(opus, seq));
        }
        this.frames = frames.build();
    }

    /**
     * @return may return an empty list to signal end of stream
     */
    public List<Frame> get() {
        long now = System.nanoTime();
        if (now >= this.nextTick) {
            synchronized (this) {
                if (now >= this.nextTick) {
                    this.nextTick += FRAME_INTERVAL_NANOS * this.frameCount - 500_000L;
                    this.tick();
                }
            }
        }
        return this.frames;
    }

    @ApiStatus.Internal
    public void waitNextTick() {
        long wait;
        while ((wait = this.nextTick - System.nanoTime()) > 0L) {
            LockSupport.parkNanos(wait);
        }
    }

    @Override
    public void close() {
        try (this.processor) {
        }
    }
}
