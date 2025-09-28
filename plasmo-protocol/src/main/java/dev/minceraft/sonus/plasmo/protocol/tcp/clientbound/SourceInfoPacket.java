package dev.minceraft.sonus.plasmo.protocol.tcp.clientbound;

import dev.minceraft.sonus.plasmo.protocol.tcp.TcpHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.source.SourceInfo;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.source.SourceType;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SourceInfoPacket extends TcpPlasmoPacket<SourceInfoPacket> {

    private @MonotonicNonNull SourceInfo sourceInfo;

    public SourceInfoPacket() {

    }

    @Override
    public void encode(ByteBuf buf) {
        SourceType.encode(buf, this.sourceInfo);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.sourceInfo = SourceType.decode(buf);
    }

    @Override
    public void handle(TcpHandler handler) {
        handler.handleSourceInfoPacket(this);
    }

    public SourceInfo getSourceInfo() {
        return this.sourceInfo;
    }

    public void setSourceInfo(SourceInfo sourceInfo) {
        this.sourceInfo = sourceInfo;
    }
}
