package dev.minceraft.sonus.service.rooms;
// Created by booky10 in Sonus (02:18 17.07.2025)

import dev.minceraft.sonus.service.audio.SonusAudio;
import dev.minceraft.sonus.service.player.SonusPlayer;
import dev.minceraft.sonus.common.IAudioSource;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public abstract class AbstractRoom implements IAudioSource {

    protected final UUID roomId = UUID.randomUUID();
    protected boolean isolatedHearing = true;
    protected boolean isolatedSpeaking = true;

    protected final Map<UUID, SonusPlayer> members = new ConcurrentHashMap<>();

    public final void sendAudio(@Nullable IAudioSource source, SonusAudio audio) {
        IAudioSource realSource = Objects.requireNonNullElse(source, this);
        this.sendAudio0(realSource, audio);
    }

    @ApiStatus.OverrideOnly
    protected abstract void sendAudio0(IAudioSource source, SonusAudio audio);

    @Override
    public UUID getSenderId() {
        return this.roomId;
    }

    public boolean isIsolatedHearing() {
        return this.isolatedHearing;
    }

    public void setIsolatedHearing(boolean isolatedHearing) {
        this.isolatedHearing = isolatedHearing;
    }

    public boolean isIsolatedSpeaking() {
        return this.isolatedSpeaking;
    }

    public void setIsolatedSpeaking(boolean isolatedSpeaking) {
        this.isolatedSpeaking = isolatedSpeaking;
    }
}
