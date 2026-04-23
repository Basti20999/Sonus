package dev.minceraft.sonus.web.adapter.util;
// Created by booky10 in Sonus (9:51 PM 02.03.2026)

import dev.minceraft.sonus.common.SonusConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Made for 16-bit signed PCM stereo audio data.
 */
@NullMarked
public final class AudioMixer implements AutoCloseable {

    private static final int GC_INTERVAL = 0b1111111;

    private final Map<UUID, ByteBuf> queue = new ConcurrentHashMap<>(16);
    private int tick = 0;

    private static void append(ByteBuf buf, short[] leftData, short[] rightData, float volume) {
        if (buf.readableBytes() > SonusConstants.FRAME_SIZE * SonusConstants.FRAMES_PER_SECOND * 3) {
            return; // don't queue if buffer already too large
        }
        int len = leftData.length;
        buf.ensureWritable(len * (Short.BYTES * 2));
        for (int i = 0; i < len; i++) {
            buf.writeShortLE((short) (leftData[i] * volume));
            buf.writeShortLE((short) (rightData[i] * volume));
        }
    }

    public void handle(UUID channelId, short[] leftData, short[] rightData, float volume) {
        // CHM.computeIfAbsent is atomic; buf mutations are serialised on the buffer's own lock
        ByteBuf buf = this.queue.computeIfAbsent(channelId, __ ->
                PooledByteBufAllocator.DEFAULT.buffer(SonusConstants.FRAME_SIZE * 4));
        synchronized (buf) {
            append(buf, leftData, rightData, volume);
        }
    }


    public short @Nullable [] tick(int samples) {
        boolean gc = (this.tick++ & GC_INTERVAL) == 0;
        short[] mixed = null;
        // atomic replacement of empty/drained buffers avoids a second iteration for GC
        for (var it = this.queue.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, ByteBuf> entry = it.next();
            ByteBuf buf = entry.getValue();
            synchronized (buf) {
                if (!buf.isReadable()) {
                    buf.release();
                    it.remove();
                    continue;
                }
                if (mixed == null) {
                    // lazy init, samples*2 because of stereo
                    mixed = new short[samples << 1];
                }
                // samples*2 because see above, bytes/2 because 16-bit is two bytes
                int maxStereoSamples = Math.min(samples << 1, buf.readableBytes() >> 1);
                for (int i = 0; i < maxStereoSamples; i++) {
                    // add values together
                    mixed[i] = clampedAdd(buf.readShortLE(), mixed[i]);
                }
                if (gc) {
                    buf.discardSomeReadBytes();
                }
            }
        }
        return mixed;
    }

    private static short clampedAdd(short a, short b) {
        int r = a + b;
        if (r > Short.MAX_VALUE) {
            return Short.MAX_VALUE;
        } else if (r < Short.MIN_VALUE) {
            return Short.MIN_VALUE;
        }
        return (short) r;
    }

    public void clear() {
        // drain via iterator so handle() on another thread can't race a removal
        for (var it = this.queue.entrySet().iterator(); it.hasNext(); ) {
            ByteBuf buf = it.next().getValue();
            synchronized (buf) {
                buf.release();
            }
            it.remove();
        }
    }

    @Override
    public void close() {
        this.clear();
    }
}
