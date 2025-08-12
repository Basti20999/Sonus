package dev.minceraft.sonus.svc.protocol.voice;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class GroupSoundSvcPacket extends SoundSvcPacket<GroupSoundSvcPacket> {

    @Override
    public void handle(IVoiceSvcHandler handler) {
        handler.handleGroupSoundPacket(this);
    }
}
