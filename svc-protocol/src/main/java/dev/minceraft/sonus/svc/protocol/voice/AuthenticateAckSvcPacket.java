package dev.minceraft.sonus.svc.protocol.voice;

import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AuthenticateAckSvcPacket extends SvcVoicePacket<AuthenticateAckSvcPacket> {

    @Override
    public void handle(IVoiceSvcHandler handler) {
        handler.handleAuthenticateAck(this);
    }

    @Override
    public void encode(ByteBuf buf) {

    }

    @Override
    public void decode(ByteBuf buf) {

    }
}
