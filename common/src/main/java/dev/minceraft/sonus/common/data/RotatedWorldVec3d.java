package dev.minceraft.sonus.common.data;

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;

public class RotatedWorldVec3d extends WorldVec3d {

    protected final float yaw;
    protected final float pitch;

    public RotatedWorldVec3d(double x, double y, double z, Key dimension, float yaw, float pitch) {
        super(x, y, z, dimension);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static RotatedWorldVec3d read(ByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        Key dimension = DataTypeUtil.readKey(buf);
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        return new RotatedWorldVec3d(x, y, z, dimension, yaw, pitch);
    }

    public static void write(ByteBuf buf, RotatedWorldVec3d vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
        DataTypeUtil.writeKey(buf, vec.dimension);
        buf.writeFloat(vec.yaw);
        buf.writeFloat(vec.pitch);
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }
}
