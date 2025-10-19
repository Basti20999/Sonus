package dev.minceraft.sonus.service.rooms;
// Created by booky10 in Sonus (02:18 17.07.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.rooms.RoomAudioType;
import dev.minceraft.sonus.common.rooms.RoomType;
import dev.minceraft.sonus.service.SonusService;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public abstract class AbstractRoom implements IRoom {

    protected final SonusService service;
    protected final UUID roomId;
    protected final Map<UUID, ISonusPlayer> members = new ConcurrentHashMap<>();
    protected final RoomType roomType;
    protected RoomAudioType roomAudioType = RoomAudioType.OPEN;
    protected String name;
    protected @Nullable String password;

    public AbstractRoom(SonusService service, RoomType roomType) {
        this(service, UUID.randomUUID(), roomType);
    }

    public AbstractRoom(SonusService service, UUID roomId, RoomType roomType) {
        this.service = service;
        this.roomId = Objects.requireNonNull(roomId);
        this.roomType = roomType;
        this.name = "Room-" + this.roomId.toString().substring(0, 5);
    }

    @Override
    public final void sendAudio(@Nullable IAudioSource source, SonusAudio audio) {
        IAudioSource realSource = Objects.requireNonNullElse(source, this);
        this.sendAudio0(realSource, audio);
    }

    @ApiStatus.OverrideOnly
    protected abstract void sendAudio0(IAudioSource source, SonusAudio audio);

    @Override
    public boolean addMember(ISonusPlayer player) {
        if (this.members.containsKey(player.getUniqueId())) {
            return false; // Already in the room
        }
        this.members.put(player.getUniqueId(), player);
        return true;
    }

    @Override
    public boolean removeMember(ISonusPlayer player) {
        if (!this.members.containsKey(player.getUniqueId())) {
            return false; // Not in the room
        }
        this.members.remove(player.getUniqueId());
        return true;
    }

    @Override
    public UUID getId() {
        return this.roomId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    @Nullable
    public String getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    @Override
    public RoomType getRoomType() {
        return this.roomType;
    }

    @Override
    public RoomAudioType getRoomAudioType() {
        return this.roomAudioType;
    }

    public void setRoomAudioType(RoomAudioType type) {
        this.roomAudioType = Objects.requireNonNull(type);
    }

    @Override
    public Set<ISonusPlayer> getMembers() {
        return Set.copyOf(this.members.values());
    }

    @Override
    public boolean isMember(ISonusPlayer player) {
        return this.members.containsKey(player.getUniqueId());
    }
}
