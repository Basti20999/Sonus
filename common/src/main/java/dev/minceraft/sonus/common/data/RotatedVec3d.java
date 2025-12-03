package dev.minceraft.sonus.common.data;

import io.netty.buffer.ByteBuf;

public class RotatedVec3d extends Vec3d {

    protected final float yaw;
    protected final float pitch;

    public RotatedVec3d(double x, double y, double z, float yaw, float pitch) {
        super(x, y, z);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static RotatedVec3d decode(ByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        return new RotatedVec3d(x, y, z, yaw, pitch);
    }

    public static void encode(ByteBuf buf, RotatedVec3d vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
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
