package dev.minceraft.sonus.service.rooms;
// Created by booky10 in Sonus (02:20 17.07.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GroupRoom extends AbstractRoom {

    @Override
    protected void sendAudio0(IAudioSource source, SonusAudio audio) {
        for (ISonusPlayer member : this.members.values()) {
            if (member.getSenderId().equals(source.getSenderId())) {
                continue;
            }
            member.sendAudio(source, audio);
        }
    }
}
