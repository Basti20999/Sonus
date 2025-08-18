package dev.minceraft.sonus.service.player;
// Created by booky10 in Sonus (02:18 17.07.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.adapter.VoiceAdapter;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import dev.minceraft.sonus.service.rooms.AbstractRoom;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public final class SonusPlayer implements IAudioSource, ISonusPlayer {

    private final IPlatformPlayer platform;
    private @Nullable WorldVec3d position;
    private @Nullable AbstractRoom voiceRoom;
    private @Nullable VoiceAdapter voiceAdapter;

    private boolean muted;
    private boolean deafened;

    public SonusPlayer(IPlatformPlayer platform) {
        this.platform = platform;
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

    @Override
    public UUID getUniqueId() {
        return this.platform.getUniqueId();
    }

    @Override
    public String getName() {
        return this.platform.getName();
    }

    @Override
    public void sendPluginMessage(Key key, ByteBuf data) {
        this.platform.sendPluginMessage(key, data);
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
        return this.getUniqueId();
    }
}
