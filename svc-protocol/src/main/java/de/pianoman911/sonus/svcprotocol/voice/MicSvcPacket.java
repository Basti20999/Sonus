package de.pianoman911.sonus.svcprotocol.voice;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MicSvcPacket extends SvcVoicePacket<MicSvcPacket> implements ServerBound {

    private byte @MonotonicNonNull [] data;
    private boolean whispering;
    private long sequenceNumber;

    public MicSvcPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeByteArray(buf, this.data);
        buf.writeLong(this.sequenceNumber);
        buf.writeBoolean(this.whispering);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.data = DataTypeUtil.readByteArray(buf);
        this.sequenceNumber = buf.readLong();
        this.whispering = buf.readBoolean();
    }

    @Override
    public void handle(ISonusPlayer player, IVoiceSvcHandler handler) {
        handler.handleMicPacket(player, this);
    }
}
