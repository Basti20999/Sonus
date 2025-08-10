package dev.minceraft.sonus.service.rooms;
// Created by booky10 in Sonus (02:20 17.07.2025)

import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.service.player.SonusPlayer;
import dev.minceraft.sonus.common.IAudioSource;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GroupRoom extends AbstractRoom {

    @Override
    protected void sendAudio0(IAudioSource source, SonusAudio audio) {
        for (SonusPlayer value : this.members.values()) {
            value.sendAudio(source, audio);
        }
    }
}
