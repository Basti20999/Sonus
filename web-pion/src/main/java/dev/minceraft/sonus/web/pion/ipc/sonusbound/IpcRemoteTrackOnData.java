package dev.minceraft.sonus.web.pion.ipc.sonusbound;
// Created by booky10 in Sonus (8:55 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.ipc.IpcMessage;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class IpcRemoteTrackOnData extends IpcMessage {

    private final int trackId;
    private final ByteBuf data;
    private final long durationNanos;

    public IpcRemoteTrackOnData(ByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readRetainedSlice(buf.readShort()), buf.readLong());
    }

    public IpcRemoteTrackOnData(int handlerId, int trackId, ByteBuf data, long durationNanos) {
        super(handlerId);
        this.trackId = trackId;
        this.data = data;
        this.durationNanos = durationNanos;
    }

    @Override
    public void encode(ByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    public int getTrackId() {
        return this.trackId;
    }

    public ByteBuf getData() {
        return this.data;
    }

    public long getDurationNanos() {
        return this.durationNanos;
    }
}
