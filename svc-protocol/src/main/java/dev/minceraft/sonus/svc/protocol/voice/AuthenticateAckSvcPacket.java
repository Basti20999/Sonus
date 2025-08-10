package dev.minceraft.sonus.svc.protocol.voice;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AuthenticateAckSvcPacket extends SvcVoicePacket<AuthenticateAckSvcPacket> implements ClientBound {

    @Override
    public void handle(ISonusPlayer player, IVoiceSvcHandler handler) {
        handler.handleAuthenticateAck(player, this);
    }

    @Override
    public void encode(ByteBuf buf) {

    }

    @Override
    public void decode(ByteBuf buf) {

    }
}
