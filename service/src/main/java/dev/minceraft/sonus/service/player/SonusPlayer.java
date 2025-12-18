package dev.minceraft.sonus.service.player;
// Created by booky10 in Sonus (02:18 17.07.2025)

import com.google.common.base.Preconditions;
import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.data.Vec3d;
import dev.minceraft.sonus.common.data.WorldRotatedVec3d;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import dev.minceraft.sonus.protocol.meta.MetaRegistry;
import dev.minceraft.sonus.protocol.meta.agentbound.PlayerConnectionStateMessage;
import dev.minceraft.sonus.protocol.meta.agentbound.TriggerCommandUpdateMessage;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.commands.CommandSender;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import dev.minceraft.sonus.service.processing.nodes.AgcNode;
import dev.minceraft.sonus.service.processing.util.SpatialNormProcessor;
import dev.minceraft.sonus.service.rooms.ServerRoom;
import dev.minceraft.sonus.service.server.SonusServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_BYPASS_GROUP_PASSWORD;
import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_VOICE_LISTEN;
import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_VOICE_SPEAK;
import static dev.minceraft.sonus.common.SonusConstants.PLUGIN_MESSAGE_CHANNEL_KEY;

@NullMarked
public final class SonusPlayer implements ISonusPlayer, CommandSender, AutoCloseable {

    private final SonusService service;
    private final IPlatformPlayer platform;
    // keep track of which rooms this player is in
    private final Map<UUID, IRoom> voiceRooms = new ConcurrentHashMap<>();
    private final AtomicLong sequenceNumber = new AtomicLong(-1L); // audio input sequence number
    private @Nullable SonusAdapter sonusAdapter;
    private @Nullable IRoom serverRoom;
    private @Nullable IRoom prevPrimaryRoom; // hack to fix state updates
    private @Nullable IRoom primaryRoom;
    // visibility states of other players
    private Map<UUID, SonusPlayerState> perPlayerStates = Map.of();
    private @Nullable AgcNode agcNode; // automatic gain control

    // metadata sent by the backend server agent
    private @Nullable WorldRotatedVec3d position;
    private @Nullable String team;

    // player state
    private boolean connected;
    private boolean muted;
    private boolean deafened;

    private long lastKeepAlive = System.currentTimeMillis();

    // track server reference
    private @Nullable SonusServer server;

    public SonusPlayer(SonusService service, IPlatformPlayer platform) {
        this.service = service;
        this.platform = platform;
    }

    @Override
    public void handleAudioInput(SonusAudio audio) {
        if (this.muted || this.deafened
                || !this.platform.hasPermission(PERMISSION_VOICE_SPEAK, true)) {
            return;
        }

        // Prevents sequence number regression, e.g. after a reconnect
        long sequence = this.sequenceNumber.updateAndGet(
                num -> Math.max(num, audio.sequenceNumber()) + 1L);
        SonusAudio sequencedAudio = audio.withSequenceNumber(sequence);
        this.processAudioInput(sequencedAudio);
        this.handleRoomBroadcast(room -> room.sendAudio(this, sequencedAudio));
    }

    @Override
    public void handleAudioInputEnd(long sequence) {
        if (!this.platform.hasPermission(PERMISSION_VOICE_SPEAK, true)) {
            return;
        }
        long finalSequence = this.sequenceNumber.updateAndGet(
                num -> Math.max(num, sequence));
        this.handleRoomBroadcast(room -> room.sendAudioEnd(this, finalSequence));
    }

    private void processAudioInput(SonusAudio audio) {
        if (audio.isZeroLength()) {
            return; // don't process zero-length audio
        }
        if (this.service.getConfig().agcEnabled()) {
            // do automatic gain control on input audio to prevent destruction of ears
            if (this.agcNode == null) {
                this.agcNode = new AgcNode();
            }
            this.agcNode.process(audio);
        }
    }

    private void handleRoomBroadcast(Consumer<IRoom> consumer) {
        // first, process all rooms which are configured to prevent audio input
        // being passed to other rooms
        boolean preventSpeakToOthers = false;
        for (IRoom room : this.voiceRooms.values()) {
            if (!room.getRoomAudioType().isSpeakToOthers()) {
                consumer.accept(room);
                preventSpeakToOthers = true;
            }
        }
        // if no rooms exist which prevent audio input being passed, send audio to all rooms
        if (!preventSpeakToOthers) {
            for (IRoom room : this.voiceRooms.values()) {
                consumer.accept(room);
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
            WorldRotatedVec3d thisPosition = this.getPosition();
            WorldRotatedVec3d thatPosition = source.getPosition();
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
        } else if (!spatial && state.staticHidden()) {
            return false; // player is hidden for static audio
        } else if (spatial && state.spatialHidden()) {
            return false; // player is hidden for spatial audio
        }
        return true;
    }

    @Override
    public void setKeepAlive(long timestamp) {
        this.lastKeepAlive = timestamp;
    }

    @Override
    public long getLastKeepAlive() {
        return this.lastKeepAlive;
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
    public void sendSpatialNormedAudio(IAudioSource source, SonusAudio audio) {
        if (this.sonusAdapter == null || !this.canHear(source, true)) {
            return;
        }
        Vec3d pos = SpatialNormProcessor.normalizeAudio(this.service, this, source, audio);
        if (pos != null) { // if the position is null, the processor decided to cancel the audio packet
            this.sonusAdapter.sendSpatialAudio(this, source, audio, pos);
        }
    }

    @Override
    public void sendAudioEnd(IAudioSource source, long sequence) {
        if (this.sonusAdapter == null || !this.canHear(source, true)) {
            return;
        }
        this.sonusAdapter.sendAudioEnd(this, source, sequence);
    }

    @Override
    public boolean canAccessRoom(IRoom room, @Nullable String password) {
        // check password
        if (!this.hasPermission(PERMISSION_BYPASS_GROUP_PASSWORD, false)) {
            if (!Objects.equals(room.getPassword(), password)) {
                return false; // wrong password (and no bypass permission)
            }
        }
        if (room instanceof ServerRoom) {
            return false; // don't allow accessing server rooms manually
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
            this.prevPrimaryRoom = room;
            this.updateState();
            this.prevPrimaryRoom = null;
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
        this.prevPrimaryRoom = prevRoom;
        this.primaryRoom = room;
        if (room != null) {
            this.joinRoom(room);
        }
        // trigger state update
        this.updateState();
        this.prevPrimaryRoom = null;

        this.updateCommands();
    }

    @Override
    public UUID getUniqueId(@Nullable ISonusPlayer viewer) {
        IPlatformPlayer platformViewer = viewer != null ? ((SonusPlayer) viewer).platform : null;
        return this.platform.getUniqueId(platformViewer);
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
    public void sendBackendPluginMessage(Key key, ByteBuf data) {
        this.platform.sendBackendPluginMessage(key, data);
    }

    @Override
    public @Nullable WorldRotatedVec3d getPosition() {
        return this.position;
    }

    public void setPosition(@Nullable WorldRotatedVec3d position) {
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
        this.setConnected(connected, true);
    }

    public void updateCommands() {
        // trigger command updates, some commands are not available if connected or only if connected
        //
        // velocity doesn't support directly updating commands, so we need
        // to tell the agent to fire a command update packet; still, some platforms may
        // support this feature, so leave it up to the platform
        if (this.service.getPlatform().isCommandUpdateSupported()) {
            this.platform.updateCommands();
        } else {
            this.sendMetaPacket(new TriggerCommandUpdateMessage(this.getUniqueId()));
        }
    }

    public void setConnected(boolean connected, boolean sendToAgent) {
        if (this.connected == connected) {
            return; // no change
        }
        this.connected = connected;
        this.lastKeepAlive = System.currentTimeMillis();

        this.service.getEventManager().onConnectionState(this);
        if (sendToAgent) {
            PlayerConnectionStateMessage packet = new PlayerConnectionStateMessage();
            packet.setPlayerId(this.getUniqueId());
            packet.setConnected(connected);

            this.sendMetaPacket(packet);
        }
    }

    public void sendMetaPacket(IMetaMessage packet) {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
        try {
            MetaRegistry.REGISTRY.encode(buf, packet);
            this.sendBackendPluginMessage(PLUGIN_MESSAGE_CHANNEL_KEY, buf.retain());
        } finally {
            buf.release();
        }
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
        this.updateServer();
    }

    @Override
    public boolean hasPermission(String permission, boolean defaultValue) {
        return this.platform.hasPermission(permission, defaultValue);
    }

    @Override
    public void sendMessage(Component component) {
        this.platform.sendMessage(component);
    }

    @Override
    public boolean isOnline() {
        return this.platform.isOnline();
    }

    @Override
    public boolean canSee(ISonusPlayer target) {
        if (this == target) {
            return true; // the player can always view themselves, regardless of what happens
        }
        // if the target is in a primary room, this player should be able to
        // see them in the group selection; we therefore need to send status updates
        if (target.getPrimaryRoom() == null && ((SonusPlayer) target).prevPrimaryRoom != this.primaryRoom) {
            // if the target isn't in a primary room and the server doesn't match, don't send updates
            if (!Objects.equals(this.getServerId(), target.getServerId())) {
                return false;
            }
        }
        // check whether the player is hidden or not; if no state is set, ask platform for fallback
        SonusPlayerState state = this.perPlayerStates.get(target.getUniqueId());
        if (state != null) {
            return !state.staticHidden() || !state.spatialHidden();
        }
        return this.platform.canSeeFallback(((SonusPlayer) target).platform);
    }

    @Override
    public Component renderComponent(Component component) {
        return this.platform.renderComponent(component);
    }

    @Override
    public String renderPlainComponent(Component component) {
        return this.platform.renderPlainComponent(component);
    }

    @ApiStatus.Internal
    public void setStates(Map<UUID, SonusPlayerState> states) {
        this.sendMessage(Component.text(states.toString()));
        if (!states.equals(this.perPlayerStates)) {
            this.perPlayerStates = Map.copyOf(states);
        }
    }

    @Override
    public void disconnect() {
        this.service.getEventManager().onPlayerDisconnect(this);

        // leave all rooms (includes primary + server rooms)
        for (IRoom room : Set.copyOf(this.voiceRooms.values())) {
            this.leaveRoom(room);
        }
        // invalidate server
        this.updateServer(null);

        // mark as disconnected
        this.setConnected(false);
        this.updateState();
    }

    public void updateServer() {
        UUID serverId = this.platform.getServerId();
        if (serverId != null) {
            this.updateServer(this.service.getPlayerManager().getServer(serverId));
        } else {
            this.updateServer(null);
        }
    }

    public void updateServer(@Nullable SonusServer server) {
        if (server == this.server) {
            return; // nothing changed
        }
        if (this.server != null) {
            this.server.onQuit(this);
        }
        this.server = server;
    }

    @Override
    public void close() {
        try (AgcNode ignoredAgcNode = this.agcNode) {
            // NO-OP
        } finally {
            this.agcNode = null;
        }
    }

    public void tickKeepAlive(long currentTime) {
        if (this.sonusAdapter != null) {
            this.sonusAdapter.sendKeepAlive(this, currentTime);
        }
        long keepAliveTimeoutMs = this.service.getConfig().getKeepAliveTimeoutMs();
        if (this.lastKeepAlive + keepAliveTimeoutMs < currentTime) {
            this.disconnect();
        }
    }
}
