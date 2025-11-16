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
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import dev.minceraft.sonus.service.processing.AudioProcessor;
import dev.minceraft.sonus.service.processing.nodes.AgcNode;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
    private @Nullable SonusAdapter sonusAdapter;

    // keep track of which rooms this player is in
    private final Map<UUID, IRoom> voiceRooms = new ConcurrentHashMap<>();
    private @Nullable IRoom serverRoom;
    private @Nullable IRoom primaryRoom;

    // visibility states of other players
    private Map<UUID, SonusPlayerState> perPlayerStates = Map.of();

    // audio input sequence number
    private final AtomicLong sequenceNumber = new AtomicLong();
    // automatic gain control
    private @MonotonicNonNull AudioProcessor processor;
    private @MonotonicNonNull AgcNode agcNode;

    // metadata sent by the backend server agent
    private @Nullable WorldVec3d position;
    private @Nullable String team;

    // player state
    private boolean connected;
    private boolean muted;
    private boolean deafened;

    public SonusPlayer(SonusService service, IPlatformPlayer platform) {
        this.service = service;
        this.platform = platform;
    }

    @Override
    public void handleAudioInput(SonusAudio audio) {
        if (this.muted || !this.platform.hasPermission(PERMISSION_VOICE_SPEAK, true)) {
            return;
        }

        // Prevents sequence number regression, e.g. after a reconnect
        long sequence = this.sequenceNumber.updateAndGet(
                num -> Math.max(num, audio.sequenceNumber()) + 1L);
        SonusAudio sequencedAudio = audio.withSequenceNumber(sequence);

        SonusAudio processedAudio = this.processAudioInput(sequencedAudio);
        this.broadcastAudioInput(processedAudio);
    }

    private SonusAudio processAudioInput(SonusAudio audio) {
        if (!this.service.getConfig().agcEnabled()) {
            return audio;
        }
        // do automatic gain control on input audio to prevent destruction of ears
        if (this.agcNode == null) {
            this.agcNode = new AgcNode();
        }
        if (this.processor == null) {
            this.processor = new AudioProcessor(this.service);
        }
        return audio.withData(this.processor.process(audio.data(), this.agcNode));
    }

    private void broadcastAudioInput(SonusAudio audio) {
        // first, process all rooms which are configured to prevent audio input
        // being passed to other rooms
        boolean preventSpeakToOthers = false;
        for (IRoom room : this.voiceRooms.values()) {
            if (!room.getRoomAudioType().isSpeakToOthers()) {
                room.sendAudio(this, audio);
                preventSpeakToOthers = true;
            }
        }
        // if no rooms exist which prevent audio input being passed, send audio to all rooms
        if (!preventSpeakToOthers) {
            for (IRoom room : this.voiceRooms.values()) {
                room.sendAudio(this, audio);
            }
        }
    }

    private boolean canHear(IAudioSource source, boolean spatial) {
        if (this.deafened || !this.platform.hasPermission(PERMISSION_VOICE_LISTEN, true)) {
            return false;
        } else if (this == source) {
            return false; // never make players listen to themselves
        }
        IRoom primaryRoom = this.getPrimaryRoom();
        if (primaryRoom != null) {
            // if this player is in an isolated room, the player won't be
            // able to listen to player audio from outside of this room
            if (!primaryRoom.getRoomAudioType().isListenToOthers()
                    && source instanceof ISonusPlayer other
                    && other.getPrimaryRoom() != primaryRoom) {
                return false;
            }
        }
        if (spatial) {
            // if this is spatial audio, check the server matches
            if (!Objects.equals(source.getServerId(), this.getServerId())) {
                return false;
            }
            WorldVec3d thisPosition = this.getPosition();
            WorldVec3d thatPosition = source.getPosition();
            if (thisPosition != null && thatPosition != null) {
                if (!thisPosition.getDimension().equals(thatPosition.getDimension())) {
                    return false; // dimensions don't match
                }
                double voiceRange = this.service.getConfig().getVoiceChatRange();
                if (thisPosition.distanceSquared(thatPosition) >= voiceRange * voiceRange) {
                    return false; // too far away
                }
            }
        }
        // check if player is hidden
        SonusPlayerState state = this.perPlayerStates.get(source.getSenderId());
        if (state == null) {
            if (source instanceof SonusPlayer sonusSource) {
                // call platform-specific fallback when there is no state set
                return this.platform.canSeeFallback(sonusSource.platform);
            }
        } else if (state.tablistHidden()) {
            return false; // player is completely hidden
        } else if (spatial && state.hidden()) {
            return false; // player is hidden for spatial audio
        }
        return true;
    }

    @Override
    public void sendStaticAudio(IAudioSource source, SonusAudio audio) {
        if (this.sonusAdapter != null && this.canHear(source, false)) {
            this.sonusAdapter.sendStaticAudio(this, source, audio);
        }
    }

    @Override
    public void sendSpatialAudio(IAudioSource source, SonusAudio audio, Vec3d position) {
        if (this.sonusAdapter != null && this.canHear(source, true)) {
            this.sonusAdapter.sendSpatialAudio(this, source, audio, position);
        }
    }

    @Override
    public void sendSpatialAudio(IAudioSource source, SonusAudio audio) {
        if (this.sonusAdapter != null && this.canHear(source, true)) {
            this.sonusAdapter.sendSpatialAudio(this, source, audio);
        }
    }

    @Override
    public boolean canAccessRoom(IRoom room, @Nullable String password) {
        // check password
        if (!this.hasPermission(PERMISSION_BYPASS_GROUP_PASSWORD, false)) {
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
    public String getName(@Nullable ISonusPlayer viewer) {
        IPlatformPlayer platformViewer = viewer != null ? ((SonusPlayer) viewer).platform : null;
        return this.platform.getName(platformViewer);
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
    public void handleConnect() {
        // switch server room
        UUID serverId = this.getServerId();
        IRoom room = serverId != null ? this.service.getRoomManager().getRoom(serverId) : null;
        this.setServerRoom(room);
    }

    @Override
    public void ensureTabListed(ISonusPlayer target) {
        this.platform.ensureTabListed(((SonusPlayer) target).platform);
    }

    @Override
    public void updateState() {
        this.service.getEventManager().onPlayerStateUpdate(this);
    }

    @Override
    public boolean hasPermission(String permission, boolean defaultValue) {
        return this.platform.hasPermission(permission, defaultValue);
    }

    @Override
    public boolean canSee(ISonusPlayer target) {
        if (this == target) {
            return true; // the player can always view themselves, regardless of what happens
        }
        // if the target is in a primary room, this player should be able to
        // see them in the group selection; we therefore need to send status updates
        if (target.getPrimaryRoom() == null) {
            // if the target isn't in a primary room and the server doesn't match, don't send updates
            if (!Objects.equals(this.getServerId(), target.getServerId())) {
                return false;
            }
        }
        // check whether the player is fully hidden or not; if no state is set, default to hidden,
        // to not expose vanished players in groups in server switch
        SonusPlayerState state = this.perPlayerStates.get(target.getUniqueId());
        if (state != null) {
            return !state.tablistHidden();
        }
        return this.platform.canSeeFallback(((SonusPlayer) target).platform);
    }

    @ApiStatus.Internal
    public void setStates(Map<UUID, SonusPlayerState> states) {
        if (!states.equals(this.perPlayerStates)) {
            this.perPlayerStates = Map.copyOf(states);
            System.out.println("[" + this.getName() + "] STATES UPDATE: " + states);
        }
    }

    public void handleQuit() {
        // leave all rooms (includes primary + server rooms)
        for (IRoom room : Set.copyOf(this.voiceRooms.values())) {
            this.leaveRoom(room);
        }
    }
}
