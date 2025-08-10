package dev.minceraft.sonus.svc.protocol.voice;


import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.svc.protocol.AbstractSvcPacket;

public abstract class SvcVoicePacket<T extends SvcVoicePacket<?>> extends AbstractSvcPacket {

    protected SvcVoicePacket() {
    }

    public abstract void handle(ISonusPlayer player, IVoiceSvcHandler handler);
}
