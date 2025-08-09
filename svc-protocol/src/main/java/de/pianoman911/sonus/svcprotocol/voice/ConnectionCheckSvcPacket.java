package de.pianoman911.sonus.svcprotocol.voice;

import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ServerBound;
import io.netty.buffer.ByteBuf;

public class ConnectionCheckSvcPacket extends SvcVoicePacket<ConnectionCheckSvcPacket> implements ServerBound {

    public ConnectionCheckSvcPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
    }

    @Override
    public void decode(ByteBuf buf) {
    }

    @Override
    public void handle(ISonusPlayer player, IVoiceSvcHandler handler) {
        handler.handleConnectionCheck(player, this);
    }
}
