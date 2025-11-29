package dev.minceraft.sonus.svc.protocol.voice.clientbound;

import dev.minceraft.sonus.svc.protocol.voice.IVoiceSvcHandler;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GroupSoundSvcPacket extends SoundSvcPacket {

    @Override
    public void handle(IVoiceSvcHandler handler) {
        handler.handleGroupSoundPacket(this);
    }
}
