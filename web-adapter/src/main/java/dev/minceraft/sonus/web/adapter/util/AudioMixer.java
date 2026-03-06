package dev.minceraft.sonus.web.adapter.util;
// Created by booky10 in Sonus (9:51 PM 02.03.2026)

import dev.minceraft.sonus.common.SonusConstants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.NullMarked;

import java.nio.ByteBuffer;
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
        synchronized (this.lock) {
            append(this.getBuffer(channelId), leftData, rightData, volume);
        }
    }

    public boolean tick(ByteBuffer nioBuf) {
        // java nio bytebuffers are always big endian, so wrap using netty
        int capacity = nioBuf.capacity();
        ByteBuf nettyBuf = Unpooled.wrappedBuffer(nioBuf);

        boolean mut = false;
        boolean gc = (this.tick++ & GC_INTERVAL) == 0;
        synchronized (this.lock) {
            for (Iterator<ByteBuf> it = this.queue.values().iterator(); it.hasNext(); ) {
                ByteBuf buf = it.next();
                if (!buf.isReadable()) {
                    buf.release();
                    it.remove();
                    continue;
                }
                mut = true;
                // fill buffer
                int maxStereoSamples = Math.min(capacity, buf.readableBytes());
                for (int i = 0; i < maxStereoSamples; i += Short.BYTES) {
                    short v = clampedAdd(buf.readShortLE(), nettyBuf.getShortLE(i));
                    nettyBuf.setShortLE(i, v);
                }
                if (gc) {
                    buf.discardSomeReadBytes();
                }
            }
        }
        return mut;
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
        this.clear();
    }
}
