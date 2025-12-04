package dev.minceraft.sonus.common.data;

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;

public class WorldRotatedVec3d extends RotatedVec3d {

    protected Key dimension;

    public WorldRotatedVec3d(double x, double y, double z, float yaw, float pitch, Key dimension) {
        super(x, y, z, yaw, pitch);
        this.dimension = dimension;
    }

    public static WorldRotatedVec3d read(ByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        Key dimension = DataTypeUtil.readKey(buf);
        return new WorldRotatedVec3d(x, y, z, yaw, pitch, dimension);
    }

    public static void write(ByteBuf buf, WorldRotatedVec3d vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
        buf.writeFloat(vec.yaw);
        buf.writeFloat(vec.pitch);
        DataTypeUtil.writeKey(buf, vec.dimension);
    }

    public Key getDimension() {
        return this.dimension;
    }
}
