package dev.minceraft.sonus.common.rooms;

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

@NullMarked
public interface IRoom extends IAudioSource {

    UUID getId();

    String getName();

    void setName(String name);

    @Nullable
    String getPassword();

    void setPassword(@Nullable String password);

    Set<ISonusPlayer> getMembers();

    boolean addMember(ISonusPlayer player);

    boolean removeMember(ISonusPlayer player);

    boolean isMember(ISonusPlayer player);

    RoomAudioType getRoomAudioType();

    RoomType getRoomType();

    void setRoomType(RoomAudioType type);

    void sendAudio(@Nullable IAudioSource source, SonusAudio audio);

    @Override
    default UUID getSenderId() {
        return this.getId();
    }
}
