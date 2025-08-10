package dev.minceraft.sonus.protocol.svc.voice;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerSoundSvcPacket extends SoundSvcPacket<PlayerSoundSvcPacket> {

    private boolean whispering;
    private float distance;

    public PlayerSoundSvcPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
        // what the fuck happened here with the field order? We can't use proper inheritance.
        DataTypeUtil.writeUniqueId(buf, this.channelId);
        DataTypeUtil.writeUniqueId(buf, this.sender);
        DataTypeUtil.writeByteArray(buf, this.data);
        buf.writeLong(this.sequenceNumber);
        buf.writeFloat(this.distance);

        byte data = 0b0;
        if (this.whispering) {
            data = setFlag(data, WHISPER_MASK);
        }
        if (this.category != null) {
            data = setFlag(data, HAS_CATEGORY_MASK);
        }
        buf.writeByte(data);
        if (this.category != null) {
            Utf8String.write(buf, this.category);
        }
    }

    @Override
    public void decode(ByteBuf buf) {
        this.channelId = DataTypeUtil.readUniqueId(buf);
        this.sender = DataTypeUtil.readUniqueId(buf);
        this.data = DataTypeUtil.readByteArray(buf);
        this.sequenceNumber = buf.readLong();
        this.distance = buf.readFloat();

        byte flag = buf.readByte();
        this.whispering = hasFlag(flag, WHISPER_MASK);
        this.category = hasFlag(flag, HAS_CATEGORY_MASK) ? Utf8String.read(buf, 16) : null;
    }

    @Override
    public void handle(ISonusPlayer player, IVoiceSvcHandler handler) {
        handler.handlePlayerSoundPacket(player, this);
    }

    public boolean isWhispering() {
        return this.whispering;
    }

    public void setWhispering(boolean whispering) {
        this.whispering = whispering;
    }

    public float getDistance() {
        return this.distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
