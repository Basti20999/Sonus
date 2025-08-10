package dev.minceraft.sonus.svc.protocol.voice;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ConnectionCheckAckSvcPacket extends SvcVoicePacket<ConnectionCheckAckSvcPacket> {

    public ConnectionCheckAckSvcPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
    }

    @Override
    public void decode(ByteBuf buf) {
    }

    @Override
    public void handle(ISonusPlayer player, IVoiceSvcHandler handler) {
        handler.handleConnectionCheckAck(player, this);
    }
}
