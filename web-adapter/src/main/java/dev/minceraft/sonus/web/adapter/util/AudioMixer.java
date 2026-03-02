package dev.minceraft.sonus.web.adapter.util;
// Created by booky10 in Sonus (9:51 PM 02.03.2026)

import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.common.util.AudioConversionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Made for 16-bit signed PCM stereo audio data.
 */
@NullMarked
public final class AudioMixer implements AutoCloseable {

    private static final int GC_INTERVAL = 0b1111111;

    private final Object lock = new Object();
    private final Map<UUID, ByteBuf> queue = new HashMap<>();
    private int tick = 0;

    private ByteBuf getBuffer(UUID channelId) {
        return this.queue.computeIfAbsent(channelId, __ ->
                PooledByteBufAllocator.DEFAULT.buffer(SonusConstants.FRAME_SIZE * 4));
    }

    private static void append(ByteBuf buf, short[] leftData, short[] rightData) {
        int len = leftData.length;
        buf.ensureWritable(len * 2 * Short.BYTES);
        for (int i = 0; i < len; i++) {
            buf.writeShortLE(leftData[i]);
            buf.writeShortLE(rightData[i]);
        }
    }

    public void handle(UUID channelId, short[] leftData, short[] rightData) {
        synchronized (this.lock) {
            append(this.getBuffer(channelId), leftData, rightData);
        }
    }

    public byte @Nullable [] tickAsBytes(int samples) {
        short[] samplesArr = this.tick(samples);
        if (samplesArr != null) {
            return AudioConversionUtil.shortsToBytes(samplesArr);
        }
        return null;
    }

    public short @Nullable [] tick(int samples) {
        boolean gc = (this.tick++ & GC_INTERVAL) == 0;
        short[] mixed = null;
        synchronized (this.lock) {
            for (Iterator<ByteBuf> it = this.queue.values().iterator(); it.hasNext(); ) {
                ByteBuf buf = it.next();
                if (!buf.isReadable()) {
                    buf.release();
                    it.remove();
                    continue;
                }
                if (mixed == null) {
                    mixed = new short[samples * 2];
                }
                int maxStereoSamples = Math.min(samples * 2, buf.readableBytes());
                for (int i = 0; i < maxStereoSamples; i++) {
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
        synchronized (this.lock) {
            for (ByteBuf buf : this.queue.values()) {
                buf.release();
            }
            this.queue.clear();
        }
    }

    @Override
    public void close() {
        this.clear()
    }
}
