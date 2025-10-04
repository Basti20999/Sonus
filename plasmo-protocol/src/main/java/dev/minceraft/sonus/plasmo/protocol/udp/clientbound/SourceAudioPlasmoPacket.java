package dev.minceraft.sonus.plasmo.protocol.udp.clientbound;


import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.plasmo.protocol.udp.UdpHandler;
import dev.minceraft.sonus.plasmo.protocol.udp.bothbound.BaseAudioPlasmoPacket;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class SourceAudioPlasmoPacket extends BaseAudioPlasmoPacket<SourceAudioPlasmoPacket> {

    private @MonotonicNonNull UUID sourceId;
    private byte sourceStats;
    private short distance;

    public SourceAudioPlasmoPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
        super.encode(buf);
        DataTypeUtil.writeUniqueId(buf, this.sourceId);
        buf.writeByte(this.sourceStats);
        buf.writeShort(this.distance);
    }

    @Override
    public void decode(ByteBuf buf) {
        super.decode(buf);
        this.sourceId = DataTypeUtil.readUniqueId(buf);
        this.sourceStats = buf.readByte();
        this.distance = buf.readShort();
    }

    @Override
    public void handle(UdpHandler handler) {
        handler.handleSourceAudioPacket(this);
    }

    public UUID getSourceId() {
        return this.sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public byte getSourceStats() {
        return this.sourceStats;
    }

    public void setSourceStats(byte sourceStats) {
        this.sourceStats = sourceStats;
    }

    public short getDistance() {
        return this.distance;
    }

    public void setDistance(short distance) {
        this.distance = distance;
    }
}
