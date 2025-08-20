package dev.minceraft.sonus.service.platform;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;

@NullMarked
public interface IServicePlatform {

    Path getDataPath();

    @Nullable
    IPlatformPlayer getPlayer(UUID uniqueId);

    void registerPluginChannel(Key channel);
}
