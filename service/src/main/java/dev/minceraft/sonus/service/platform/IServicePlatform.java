package dev.minceraft.sonus.service.platform;

import dev.minceraft.sonus.service.rooms.AbstractRoom;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@NullMarked
public interface IServicePlatform {

    Path getDataPath();

    @Nullable
    IPlatformPlayer getPlayer(UUID uniqueId);

    void registerPluginChannel(Key channel);

    Set<IServer> getServers();

    AbstractRoom provideRoom(IServer server);
}
