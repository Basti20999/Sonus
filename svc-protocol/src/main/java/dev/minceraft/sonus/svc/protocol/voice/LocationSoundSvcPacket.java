package dev.minceraft.sonus.svc.protocol.voice;

import dev.minceraft.sonus.common.data.Vec3d;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LocationSoundSvcPacket extends SoundSvcPacket<LocationSoundSvcPacket> {

    protected @MonotonicNonNull Vec3d location;
    protected float distance;

    public LocationSoundSvcPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
        // what the fuck happened here with the field order? We can't use proper inheritance.
        DataTypeUtil.writeUniqueId(buf, this.channelId);
        DataTypeUtil.writeUniqueId(buf, this.sender);
        Vec3d.write(buf, this.location);
        DataTypeUtil.VAR_INT.writeByteArray(buf, this.data);
        buf.writeLong(this.sequenceNumber);
        buf.writeFloat(this.distance);

        byte data = 0b0;
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
        this.location = Vec3d.read(buf); // local field
        this.data = DataTypeUtil.VAR_INT.readByteArray(buf);
        this.sequenceNumber = buf.readLong();
        this.distance = buf.readFloat(); // local field

        byte flag = buf.readByte();
        this.category = hasFlag(flag, HAS_CATEGORY_MASK) ? Utf8String.read(buf, 16) : null;
    }

    public Vec3d getLocation() {
        return this.location;
    }

    public void setLocation(Vec3d location) {
        this.location = location;
    }

    public float getDistance() {
        return this.distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public void handle(IVoiceSvcHandler handler) {
        handler.handleLocationSoundPacket(this);
    }
}
