package dev.minceraft.sonus.svc.protocol.voice;

import dev.minceraft.sonus.common.protocol.util.PacketDirection;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class KeepAliveSvcPacket extends SvcVoicePacket<KeepAliveSvcPacket> {

    private @MonotonicNonNull PacketDirection direction;

    public KeepAliveSvcPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
    }

    @Override
    public void decode(ByteBuf buf) {
    }

    @Override
    public void handle(IVoiceSvcHandler handler) {
        handler.handleKeepAlivePacket(this);
    }
}
