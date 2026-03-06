package dev.minceraft.sonus.web.pion.ipc.pionbound;
// Created by booky10 in Sonus (8:37 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.ipc.IpcMessage;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class IpcLocalTrackSendData extends IpcMessage {

    private final int trackId;
    private final ByteBuf data;
    private final long durationNanos;

    public IpcLocalTrackSendData(int handlerId, int trackId, ByteBuf data, long durationNanos) {
        super(handlerId);
        this.trackId = trackId;
        this.data = data;
        this.durationNanos = durationNanos;
    }

    @Override
    public void encode(ByteBuf buf) {
        super.encode(buf);
        buf.writeInt(this.trackId);
        try {
            buf.writeShort(this.data.readableBytes());
            buf.writeBytes(this.data);
        } finally {
            this.data.release();
        }
        buf.writeLong(this.durationNanos);
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
