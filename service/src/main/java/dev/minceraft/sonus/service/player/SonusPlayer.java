package dev.minceraft.sonus.service.player;
// Created by booky10 in Sonus (02:18 17.07.2025)

import com.google.common.base.Preconditions;
import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import dev.minceraft.sonus.service.rooms.GroupRoom;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public final class SonusPlayer implements ISonusPlayer {

    private static final GroupRoom TEST = new GroupRoom();

    private final IPlatformPlayer platform;
    private final Map<UUID, IRoom> voiceRooms = new ConcurrentHashMap<>();
    private @Nullable IRoom customRoom;
    private @Nullable WorldVec3d position;
    private @Nullable SonusAdapter sonusAdapter;

    private boolean muted;
    private boolean deafened;

    public SonusPlayer(IPlatformPlayer platform) {
        this.platform = platform;

        //this.joinRoom(TEST);
    }

    @Override
    public void handleAudioInput(SonusAudio audio) {
        if (this.muted) {
            return;
        }
        for (IRoom room : this.voiceRooms.values()) {
            room.sendAudio(this, audio);
        }
        // TODO broadcast to nearby players wo can view this player
    }

    @Override
    public void sendAudio(IAudioSource source, SonusAudio audio) {
        if (this.sonusAdapter != null && !this.deafened) {
            this.sonusAdapter.sendAudio(this, source, audio);
        }
    }

    @Override
    public void joinRoom(IRoom room) {
        Preconditions.checkNotNull(room, "Room cannot be null");
        if (this.voiceRooms.containsKey(room.getSenderId())) {
            return; // Already in the room
        }
        this.voiceRooms.put(room.getSenderId(), room);
        room.addMember(this);
    }

    @Override
    public void leaveRoom(IRoom room) {
        Preconditions.checkNotNull(room, "Room cannot be null");
        if (!this.voiceRooms.containsKey(room.getSenderId())) {
            return; // Not in the room
        }
        this.voiceRooms.remove(room.getSenderId());
        room.removeMember(this);
    }

    @Override
    public @Nullable IRoom getCustomRoom() {
        return this.customRoom;
    }

    @Override
    public void setCustomRoom(@Nullable IRoom room) {
        this.customRoom = room;
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
    @Nullable
    public SonusAdapter getAdapter() {
        return this.sonusAdapter;
    }

    @Override
    public void setAdapter(@Nullable SonusAdapter adapter) {
        this.sonusAdapter = adapter;
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

    public Map<UUID, IRoom> getVoiceRooms() {
        return this.voiceRooms;
    }

    public @Nullable SonusAdapter getVoiceAdapter() {
        return this.sonusAdapter;
    }

    public void setVoiceAdapter(@Nullable SonusAdapter sonusAdapter) {
        this.sonusAdapter = sonusAdapter;
    }

    @Override
    public boolean isMuted() {
        return this.muted;
    }

    @Override
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    @Override
    public boolean isDeafened() {
        return this.deafened;
    }

    @Override
    public void setDeafened(boolean deafened) {
        this.deafened = deafened;
    }

    @Override
    public UUID getSenderId() {
        return this.getUniqueId();
    }
}
