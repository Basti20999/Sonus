package dev.minceraft.sonus.util;
// Created by booky10 in Sonus (01:16 17.07.2025)

import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Vec3d {

    protected final double x;
    protected final double y;
    protected final double z;

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vec3d read(ByteBuf buf) {
        return new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void write(ByteBuf buf, Vec3d vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }
}
