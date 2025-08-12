package dev.minceraft.sonus.svc.protocol.voice;


import dev.minceraft.sonus.svc.protocol.AbstractSvcPacket;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class SvcVoicePacket<T extends SvcVoicePacket<?>> extends AbstractSvcPacket<IVoiceSvcHandler> {

    protected SvcVoicePacket() {
    }
}
