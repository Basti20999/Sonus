package dev.minceraft.sonus.service.rooms;
// Created by booky10 in Sonus (02:18 17.07.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.service.player.SonusPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@NullMarked
public abstract class AbstractRoom implements IAudioSource {

    protected final UUID roomId = UUID.randomUUID();
    protected final Map<UUID, SonusPlayer> members = new ConcurrentHashMap<>();
    protected final AtomicLong sequenceNumber = new AtomicLong(0);
    protected boolean isolatedHearing = true;
    protected boolean isolatedSpeaking = true;

    public final void sendAudio(@Nullable IAudioSource source, SonusAudio audio) {
        IAudioSource realSource = Objects.requireNonNullElse(source, this);
        this.sendAudio0(realSource, audio.withSequenceNumber(this.sequenceNumber.getAndIncrement()));
    }

    @ApiStatus.OverrideOnly
    protected abstract void sendAudio0(IAudioSource source, SonusAudio audio);

    public void addMember(SonusPlayer player) {
        this.members.put(player.getUniqueId(), player);
    }

    public void removeMember(SonusPlayer player) {
        this.members.remove(player.getUniqueId());
    }

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
