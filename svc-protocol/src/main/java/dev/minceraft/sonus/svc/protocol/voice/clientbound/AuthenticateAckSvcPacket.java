package dev.minceraft.sonus.svc.protocol.voice.clientbound;

import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.voice.IVoiceSvcHandler;
import dev.minceraft.sonus.svc.protocol.voice.SvcVoicePacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AuthenticateAckSvcPacket extends SvcVoicePacket {

    @Override
    public void handle(IVoiceSvcHandler handler) {
        handler.handleAuthenticateAck(this);
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
        // NO-OP
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
        // NO-OP
    }
}
