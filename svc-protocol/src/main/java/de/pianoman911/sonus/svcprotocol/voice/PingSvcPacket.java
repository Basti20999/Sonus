package de.pianoman911.sonus.svcprotocol.voice;


import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.BothBound;
import dev.minecraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minecraft.sonus.common.protocol.util.PacketDirection;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class PingSvcPacket extends SvcVoicePacket<PingSvcPacket> implements BothBound {

    private @MonotonicNonNull PacketDirection direction;
    private @MonotonicNonNull UUID id;
    private long timestamp;

    public PingSvcPacket() {
    }

    @Override
    public PacketDirection getDirection() {
        return this.direction;
    }

    @Override
    public void setDirection(PacketDirection direction) {
        this.direction = direction;
    }

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeUniqueId(buf, this.id);
        buf.writeLong(this.timestamp);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.id = DataTypeUtil.readUniqueId(buf);
        this.timestamp = buf.readLong();
    }

    @Override
    public void handle(ISonusPlayer player, IVoiceSvcHandler handler) {
        handler.handlePingPacket(player, this);
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
