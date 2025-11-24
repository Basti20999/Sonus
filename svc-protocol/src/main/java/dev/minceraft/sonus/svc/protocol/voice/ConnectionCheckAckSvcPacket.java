package dev.minceraft.sonus.svc.protocol.voice;

import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ConnectionCheckAckSvcPacket extends SvcVoicePacket<ConnectionCheckAckSvcPacket> {

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
