package dev.minceraft.sonus.plasmo.protocol.tcp.clientbound;

import dev.minceraft.sonus.plasmo.protocol.tcp.TcpHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.VoiceSourceLine;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SourceLineRegisterPacket extends TcpPlasmoPacket<SourceLineRegisterPacket> {

    private @MonotonicNonNull VoiceSourceLine sourceLine;

    public SourceLineRegisterPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
        this.sourceLine.write(buf);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.sourceLine = new VoiceSourceLine(buf);
    }

    @Override
    public void handle(TcpHandler handler) {
        handler.handleSourceLineRegisterPacket(this);
    }

    public VoiceSourceLine getSourceLine() {
        return this.sourceLine;
    }

    public void setSourceLine(VoiceSourceLine sourceLine) {
        this.sourceLine = sourceLine;
    }
}
