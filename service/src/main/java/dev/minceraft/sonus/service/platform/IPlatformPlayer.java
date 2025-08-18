package dev.minceraft.sonus.service.platform;

import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public interface IPlatformPlayer {

    UUID getUniqueId();

    String getName();

    void sendPluginMessage(Key key, ByteBuf data);
}
