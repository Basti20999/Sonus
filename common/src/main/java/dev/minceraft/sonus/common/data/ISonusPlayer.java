package dev.minceraft.sonus.common.data;

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.rooms.IRoom;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@NullMarked
public interface ISonusPlayer extends IAudioSource {

    UUID getUniqueId();

    String getName();

    @Nullable
    String getTeam();

    Map<UUID, SonusPlayerState> getPerPlayerStates();

    @Nullable
    SonusAdapter getAdapter();

    void setAdapter(@Nullable SonusAdapter adapter);

    void sendStaticAudio(IAudioSource source, SonusAudio audio);

    void sendSpatialAudio(IAudioSource source, SonusAudio audio, Vec3d position);

    void sendSpatialAudio(IAudioSource source, SonusAudio audio);

    void joinRoom(IRoom room);

    void leaveRoom(IRoom room);

    @Nullable
    IRoom getServerRoom();

    void setServerRoom(@Nullable IRoom room);

    @Nullable
    IRoom getPrimaryRoom();

    void setPrimaryRoom(@Nullable IRoom room);

    boolean isMuted();

    void setMuted(boolean muted);

    boolean isDeafened();

    void setDeafened(boolean deafened);

    boolean isConnected();

    void setConnected(boolean connected);

    void handleAudioInput(SonusAudio audio);

    void sendPluginMessage(Key key, ByteBuf data);

    void handleConnect();

    void ensureTabListed(ISonusPlayer target);

    void updateState();

    boolean hasPermission(String permission, TriState defaultValue);

    default boolean shouldSee(ISonusPlayer target) {
        return target.getPrimaryRoom() != null ||
                Objects.requireNonNull(this.getServerId()).equals(target.getServerId()); // Server ID should never be null here
    }
}
