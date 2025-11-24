package dev.minceraft.sonus.svc.protocol.voice;


import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.PacketDirection;
import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class PingSvcPacket extends SvcVoicePacket<PingSvcPacket> {

    private @MonotonicNonNull PacketDirection direction;
    private @MonotonicNonNull UUID id;
    private long timestamp;

    public PingSvcPacket() {
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
        DataTypeUtil.writeUniqueId(buf, this.id);
        buf.writeLong(this.timestamp);
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
        this.id = DataTypeUtil.readUniqueId(buf);
        this.timestamp = buf.readLong();
    }

    @Override
    public void handle(IVoiceSvcHandler handler) {
        handler.handlePingPacket(this);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
