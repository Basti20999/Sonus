package de.pianoman911.sonus.svcprotocol.voice;

import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ClientBound;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ConnectionCheckAckSvcPacket extends SvcVoicePacket<ConnectionCheckAckSvcPacket> implements ClientBound {

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
