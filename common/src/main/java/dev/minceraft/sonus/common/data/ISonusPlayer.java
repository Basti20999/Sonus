package dev.minceraft.sonus.common.data;

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.rooms.IRoom;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@NullMarked
public interface ISonusPlayer extends IAudioSource {

    UUID getUniqueId();

    String getName();

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
    IRoom getCustomRoom();

    void setCustomRoom(@Nullable IRoom room);

    boolean isMuted();

    void setMuted(boolean muted);

    boolean isDeafened();

    void setDeafened(boolean deafened);

    void handleAudioInput(SonusAudio audio);

    void sendPluginMessage(Key key, ByteBuf data);
}
