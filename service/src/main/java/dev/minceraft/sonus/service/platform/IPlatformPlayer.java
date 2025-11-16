package dev.minceraft.sonus.service.platform;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public interface IPlatformPlayer {

    UUID getUniqueId();

    @Nullable
    UUID getServerId();

    String getName(@Nullable IPlatformPlayer viewer);

    void sendPluginMessage(Key key, ByteBuf data);

    void ensureTabListed(ISonusPlayer target);

    boolean hasPermission(String permission, boolean defaultValue);
}
