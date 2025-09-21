package dev.minceraft.sonus.service.player;
// Created by booky10 in Sonus (02:18 17.07.2025)

import com.google.common.base.Preconditions;
import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.data.Vec3d;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.rooms.RoomAudioType;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import dev.minceraft.sonus.service.processing.AudioProcessor;
import dev.minceraft.sonus.service.processing.nodes.AgcNode;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public final class SonusPlayer implements ISonusPlayer {

    private final SonusService service;
    private final IPlatformPlayer platform;
    private final Map<UUID, IRoom> voiceRooms = new ConcurrentHashMap<>();
    private final Map<UUID, SonusPlayerState> perPlayerStates = new HashMap<>();
    private final AudioProcessor processor;
    private final AgcNode agcNode = new AgcNode();
    private @Nullable IRoom serverRoom;
    private @Nullable IRoom customRoom;
    private @Nullable WorldVec3d position;
    private @Nullable SonusAdapter sonusAdapter;
    private boolean muted;
    private boolean deafened;

    public SonusPlayer(SonusService service, IPlatformPlayer platform) {
        this.service = service;
        this.platform = platform;

        this.processor = new AudioProcessor(service);
    }

    @Override
    public void handleAudioInput(SonusAudio audio) {
        if (this.muted) {
            return;
        }

        if (this.service.getConfig().agcEnabled()) {
            byte[] process = this.processor.process(audio.data(), this.agcNode);
            audio = audio.withData(process);
        }

        IRoom customRoom = this.getCustomRoom();
        if (customRoom != null) {
            if (customRoom.getRoomAudioType() != RoomAudioType.OPEN) {
                customRoom.sendAudio(this, audio);
                return;
            }
        }
        for (IRoom room : this.voiceRooms.values()) {
            room.sendAudio(this, audio);
        }
    }

    private boolean canHear(IAudioSource source) {
        if (this.deafened) {
            return false;
        }
        IRoom customRoom = this.getCustomRoom();
        if (customRoom != null) {
            if (customRoom.getRoomAudioType() == RoomAudioType.ISOLATED && source instanceof ISonusPlayer other) {
                return other.getCustomRoom() == customRoom;
            }
        }
        return true;
    }

    @Override
    public void sendStaticAudio(IAudioSource source, SonusAudio audio) {
        if (this.sonusAdapter != null && this.canHear(source)) {
            this.sonusAdapter.sendStaticAudio(this, source, audio);
        }
    }

    @Override
    public void sendSpatialAudio(IAudioSource source, SonusAudio audio, Vec3d position) {
        if (this.sonusAdapter != null && this.canHear(source)) {
            this.sonusAdapter.sendSpatialAudio(this, source, audio, position);
        }
    }

    @Override
    public void sendSpatialAudio(IAudioSource source, SonusAudio audio) {
        if (this.sonusAdapter != null && this.canHear(source)) {
            this.sonusAdapter.sendSpatialAudio(this, source, audio);
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
    public @Nullable IRoom getServerRoom() {
        return this.serverRoom;
    }

    @Override
    public void setServerRoom(@Nullable IRoom room) {
        if (this.serverRoom != null) {
            this.serverRoom.removeMember(this);
        }
        this.serverRoom = room;
    }

    @Override
    public @Nullable IRoom getCustomRoom() {
        return this.customRoom;
    }

    @Override
    public void setCustomRoom(@Nullable IRoom room) {
        if (this.customRoom != null) {
            this.customRoom.removeMember(this);
        }
        this.customRoom = room;
    }

    @Override
    public UUID getUniqueId() {
        return this.platform.getUniqueId();
    }

    @Override
    public @Nullable UUID getServerId() {
        return this.platform.getServerId();
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

    @Override
    public Map<UUID, SonusPlayerState> getPerPlayerStates() {
        return this.perPlayerStates;
    }

    @Override
    public void handleConnect() {
        this.setServerRoom(null);

        UUID serverId = this.getServerId();
        if (serverId == null) {
            return;
        }
        IRoom room = this.service.getRoomManager().getRoom(serverId);
        if (room == null) {
            return;
        }
        this.setServerRoom(room);
        this.joinRoom(room);
    }

    public void setStates(Collection<SonusPlayerState> value) {
        this.perPlayerStates.clear();
        for (SonusPlayerState state : value) {
            this.perPlayerStates.put(state.playerId(), state);
        }
    }

    public void handleQuit() {
        if (this.customRoom != null) {
            this.customRoom.removeMember(this);
            this.setCustomRoom(null);
        }
        for (IRoom room : this.voiceRooms.values()) {
            this.leaveRoom(room);
        }
    }
}
