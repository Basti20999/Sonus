package dev.minceraft.sonus.service.rooms;
// Created by booky10 in Sonus (02:20 17.07.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.service.SonusService;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class StaticRoom extends AbstractRoom {

    public StaticRoom(SonusService service, UUID roomId) {
        super(service, roomId);
    }

    @Override
    protected void sendAudio0(IAudioSource source, SonusAudio audio) {
        for (ISonusPlayer member : this.members.values()) {
            if (member.getSenderId().equals(source.getSenderId())) {
                continue;
            }
            member.sendStaticAudio(source, audio);
        }
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}
