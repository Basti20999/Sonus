package dev.minceraft.sonus.svc.protocol.voice.clientbound;

import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.voice.IVoiceSvcHandler;
import dev.minceraft.sonus.svc.protocol.voice.SvcVoicePacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ConnectionCheckAckSvcPacket extends SvcVoicePacket {

    public ConnectionCheckAckSvcPacket() {
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
    }

    @Override
    public void handle(IVoiceSvcHandler handler) {
        handler.handleConnectionCheckAck(this);
    }
}
