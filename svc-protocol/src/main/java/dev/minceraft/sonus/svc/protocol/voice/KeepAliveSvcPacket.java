package dev.minceraft.sonus.svc.protocol.voice;

import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class KeepAliveSvcPacket extends SvcVoicePacket<KeepAliveSvcPacket> {

    public static final KeepAliveSvcPacket INSTANCE = new KeepAliveSvcPacket();

    private KeepAliveSvcPacket() {
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
