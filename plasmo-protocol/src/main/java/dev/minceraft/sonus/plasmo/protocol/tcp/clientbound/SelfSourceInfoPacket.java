package dev.minceraft.sonus.plasmo.protocol.tcp.clientbound;

import dev.minceraft.sonus.plasmo.protocol.tcp.TcpHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.SelfSourceInfo;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SelfSourceInfoPacket extends TcpPlasmoPacket<SelfSourceInfoPacket> {

    private @MonotonicNonNull SelfSourceInfo sourceInfo;

    public SelfSourceInfoPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
        this.sourceInfo.write(buf);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.sourceInfo = new SelfSourceInfo(buf);
    }

    @Override
    public void handle(TcpHandler handler) {
        handler.handleSelfSourceInfoPacket(this);
    }

    public SelfSourceInfo getSourceInfo() {
        return this.sourceInfo;
    }

    public void setSourceInfo(SelfSourceInfo sourceInfo) {
        this.sourceInfo = sourceInfo;
    }
}
