package dev.minceraft.sonus.svc.protocol.voice;

import io.netty.buffer.ByteBuf;

public class ConnectionCheckSvcPacket extends SvcVoicePacket<ConnectionCheckSvcPacket> {

    public ConnectionCheckSvcPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
    }

    @Override
    public void decode(ByteBuf buf) {
    }

    @Override
    public void handle(IVoiceSvcHandler handler) {
        handler.handleConnectionCheck(this);
    }
}
