package de.pianoman911.sonus.svcprotocol.voice;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GroupSoundSvcPacket extends SoundSvcPacket<GroupSoundSvcPacket> {

    @Override
    public void handle(ISonusPlayer player, IVoiceSvcHandler handler) {
        handler.handleGroupSoundPacket(player, this);
    }
}
