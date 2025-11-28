package dev.minceraft.sonus.svc.protocol.voice.servicebound;

import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.voice.IVoiceSvcHandler;
import dev.minceraft.sonus.svc.protocol.voice.SvcVoicePacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ConnectionCheckSvcPacket extends SvcVoicePacket {

    public ConnectionCheckSvcPacket() {
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
    }

    @Override
    public void handle(IVoiceSvcHandler handler) {
        handler.handleConnectionCheck(this);
    }
}
