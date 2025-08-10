package dev.minceraft.sonus.service.player;
// Created by booky10 in Sonus (02:18 17.07.2025)

import dev.minceraft.sonus.service.adapter.VoiceAdapter;
import dev.minceraft.sonus.service.audio.SonusAudio;
import dev.minceraft.sonus.service.rooms.AbstractRoom;
import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.data.WorldVec3d;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public final class SonusPlayer implements IAudioSource {

    private final UUID playerId;
    private @Nullable WorldVec3d position;
    private @Nullable AbstractRoom voiceRoom;
    private @Nullable VoiceAdapter voiceAdapter;

    private boolean muted;
    private boolean deafened;

    public SonusPlayer(UUID playerId) {
        this.playerId = playerId;
    }

    public void handleAudioInput(SonusAudio audio) {
        if (this.muted) {
            return;
        }

        AbstractRoom room = this.voiceRoom;
        if (room != null) {
            // send audio input in voice room
            room.sendAudio(this, audio);

            if (room.isIsolatedSpeaking()) {
                return; // we're done processing
            }
        }
        // TODO broadcast to nearby players wo can view this player
    }

    public void sendAudio(IAudioSource source, SonusAudio audio) {
        if (this.voiceAdapter != null && !this.deafened) {
            this.voiceAdapter.sendAudio(source, audio);
        }
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    @Override
    public @Nullable WorldVec3d getPosition() {
        return this.position;
    }

    public void setPosition(@Nullable WorldVec3d position) {
        this.position = position;
    }

    public @Nullable AbstractRoom getVoiceRoom() {
        return this.voiceRoom;
    }

    public void setVoiceRoom(@Nullable AbstractRoom voiceRoom) {
        this.voiceRoom = voiceRoom;
    }

    public @Nullable VoiceAdapter getVoiceAdapter() {
        return this.voiceAdapter;
    }

    public void setVoiceAdapter(@Nullable VoiceAdapter voiceAdapter) {
        this.voiceAdapter = voiceAdapter;
    }

    public boolean isMuted() {
        return this.muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isDeafened() {
        return this.deafened;
    }

    public void setDeafened(boolean deafened) {
        this.deafened = deafened;
    }

    @Override
    public UUID getSenderId() {
        return this.playerId;
    }
}
