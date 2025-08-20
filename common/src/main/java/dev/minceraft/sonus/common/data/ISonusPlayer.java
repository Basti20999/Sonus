package dev.minceraft.sonus.common.data;

import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.audio.SonusAudio;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public interface ISonusPlayer {

    UUID getUniqueId();

    String getName();

    @Nullable
    SonusAdapter getAdapter();

    void setAdapter(@Nullable SonusAdapter adapter);

    boolean isMuted();

    void setMuted(boolean muted);

    boolean isDeafened();

    void setDeafened(boolean deafened);

    void handleAudioInput(SonusAudio audio);

    void sendPluginMessage(Key key, ByteBuf data);
}
