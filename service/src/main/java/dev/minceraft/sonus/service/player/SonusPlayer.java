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
import net.kyori.adventure.util.TriState;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_BYPASS_GROUP_PASSWORD;
import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_VOICE_LISTEN;
import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_VOICE_SPEAK;

@NullMarked
public final class SonusPlayer implements ISonusPlayer {

    private final SonusService service;
    private final IPlatformPlayer platform;
    private final Map<UUID, IRoom> voiceRooms = new ConcurrentHashMap<>();
    private final Map<UUID, SonusPlayerState> perPlayerStates = new HashMap<>();
    private final AtomicLong sequenceNumber = new AtomicLong();
    private @MonotonicNonNull AudioProcessor processor;
    private @MonotonicNonNull AgcNode agcNode;
    private @Nullable IRoom serverRoom;
    private @Nullable IRoom primaryRoom;
    private @Nullable WorldVec3d position;
    private @Nullable SonusAdapter sonusAdapter;
    private boolean connected;
    private boolean muted;
    private boolean deafened;
    private @Nullable String team;

    public SonusPlayer(SonusService service, IPlatformPlayer platform) {
        this.service = service;
        this.platform = platform;
    }

    @Override
    public void handleAudioInput(SonusAudio audio) {
        if (this.muted) {
            return;
        }
        if (!this.platform.hasPermission(PERMISSION_VOICE_SPEAK, TriState.TRUE)) {
            return;
        }

        // Prevents sequence number regression, e.g. after a reconnect
        if (audio.sequenceNumber() > this.sequenceNumber.get()) {
            this.sequenceNumber.set(audio.sequenceNumber());
        }
        audio = audio.withSequenceNumber(this.sequenceNumber.getAndIncrement());

        if (this.service.getConfig().agcEnabled()) {
            if (this.agcNode == null) {
                this.agcNode = new AgcNode();
            }
            if (this.processor == null) {
                this.processor = new AudioProcessor(service);
            }
            byte[] process = this.processor.process(audio.data(), this.agcNode);
            audio = audio.withData(process);
        }

        IRoom customRoom = this.getPrimaryRoom();
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
        if (!this.platform.hasPermission(PERMISSION_VOICE_LISTEN, TriState.TRUE)) {
            return false;
        }
        IRoom customRoom = this.getPrimaryRoom();
        if (customRoom != null) {
            if (customRoom.getRoomAudioType() == RoomAudioType.ISOLATED && source instanceof ISonusPlayer other) {
                return other.getPrimaryRoom() == customRoom;
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
    public boolean canAccessRoom(IRoom room, @Nullable String password) {
        // check password
        if (!this.hasPermission(PERMISSION_BYPASS_GROUP_PASSWORD, TriState.NOT_SET)) {
            if (!Objects.equals(room.getPassword(), password)) {
                return false; // wrong password (and no bypass permission)
            }
        }
        return true; // allow join
    }

    @Override
    public void joinRoom(IRoom room) {
        Preconditions.checkNotNull(room, "Room cannot be null");
        if (this.voiceRooms.putIfAbsent(room.getSenderId(), room) == null) {
            room.addMember(this);
        }
    }

    @Override
    public void leaveRoom(IRoom room) {
        Preconditions.checkNotNull(room, "Room cannot be null");
        if (this.voiceRooms.remove(room.getSenderId()) == null) {
            return; // wasn't in room
        }
        room.removeMember(this);
        if (this.primaryRoom == room) {
            this.primaryRoom = null;
            this.updateState();
        }
        if (this.serverRoom == room) {
            this.serverRoom = null;
            this.updateState();
        }
    }

    @Override
    public @Nullable IRoom getServerRoom() {
        return this.serverRoom;
    }

    @Override
    public void setServerRoom(@Nullable IRoom room) {
        if (this.primaryRoom == room) {
            return; // nothing to do
        }
        // properly leave previous room
        IRoom prevRoom = this.serverRoom;
        if (prevRoom != null) {
            this.serverRoom = null;
            this.leaveRoom(prevRoom);
        }
        // enter new room
        this.serverRoom = room;
        if (room != null) {
            this.joinRoom(room);
        }
        // trigger state update
        this.updateState();
    }

    @Override
    public @Nullable IRoom getPrimaryRoom() {
        return this.primaryRoom;
    }

    @Override
    public void setPrimaryRoom(@Nullable IRoom room) {
        if (this.primaryRoom == room) {
            return; // nothing to do
        }
        // properly leave previous room
        IRoom prevRoom = this.primaryRoom;
        if (prevRoom != null) {
            this.primaryRoom = null;
            this.leaveRoom(prevRoom);
        }
        // enter new room
        this.primaryRoom = room;
        if (room != null) {
            this.joinRoom(room);
        }
        // trigger state update
        this.updateState();
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
    public String getTeam() {
        return this.team;
    }

    public void setTeam(@Nullable String team) {
        this.team = team;
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
        if (this.deafened != deafened) {
            this.deafened = deafened;
            this.updateState();
        }
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public void setConnected(boolean connected) {
        this.connected = connected;
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
        // switch server room
        UUID serverId = this.getServerId();
        IRoom room = serverId != null ? this.service.getRoomManager().getRoom(serverId) : null;
        this.setServerRoom(room);
    }

    @Override
    public void ensureTabListed(ISonusPlayer target) {
        this.platform.ensureTabListed(target);
    }

    @Override
    public void updateState() {
        this.service.getEventManager().onPlayerStateUpdate(this);
    }

    @Override
    public boolean hasPermission(String permission, TriState defaultValue) {
        return this.platform.hasPermission(permission, defaultValue);
    }

    public void setStates(Collection<SonusPlayerState> value) {
        this.perPlayerStates.clear();
        for (SonusPlayerState state : value) {
            this.perPlayerStates.put(state.playerId(), state);
        }
    }

    public void handleQuit() {
        // leave all rooms (includes primary + server rooms)
        for (IRoom room : Set.copyOf(this.voiceRooms.values())) {
            this.leaveRoom(room);
        }
    }
}
