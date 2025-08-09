package dev.minecraft.sonus.common.data;
// Created by booky10 in Sonus (01:22 17.07.2025)

import dev.minecraft.sonus.common.protocol.util.DataTypeUtil;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WorldVec3d extends Vec3d {

    private final Key dimension;

    public WorldVec3d(double x, double y, double z, Key dimension) {
        super(x, y, z);
        this.dimension = dimension;
    }

    public static WorldVec3d read(ByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        Key dimension = DataTypeUtil.readKey(buf);
        return new WorldVec3d(x, y, z, dimension);
    }

    public static void write(ByteBuf buf, WorldVec3d vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
        DataTypeUtil.writeKey(buf, vec.dimension);
    }

    public Key getDimension() {
        return this.dimension;
    }
}
