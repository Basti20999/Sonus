package dev.minceraft.sonus.plasmo.protocol.tcp.clientbound;

import dev.minceraft.sonus.common.data.Vec3d;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPlasmoPacket;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class DistanceVisualizePacket extends TcpPlasmoPacket<DistanceVisualizePacket> {

    private int radius;
    private int hexColor;
    private @MonotonicNonNull Vec3d position;

    public DistanceVisualizePacket() {

    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(this.radius);
        buf.writeInt(this.hexColor);
        Vec3d.write(buf, this.position);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.radius = buf.readInt();
        this.hexColor = buf.readInt();
        this.position = Vec3d.read(buf);
    }

    @Override
    public void handle(TcpHandler handler) {
        handler.handleDistanceVisualizePacket(this);
    }

    public int getRadius() {
        return this.radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getHexColor() {
        return this.hexColor;
    }

    public void setHexColor(int hexColor) {
        this.hexColor = hexColor;
    }

    public Vec3d getPosition() {
        return this.position;
    }

    public void setPosition(Vec3d position) {
        this.position = position;
    }
}
