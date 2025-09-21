package dev.minceraft.sonus.service.platform;

import dev.minceraft.sonus.service.rooms.AbstractRoom;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@NullMarked
public interface IServicePlatform {

    Path getDataPath();

    @Nullable
    IPlatformPlayer getPlayer(UUID uniqueId);

    void registerPluginChannel(Key channel);

    ITask executeAsync(Runnable runnable, long period, TimeUnit unit);

    Set<IServer> getServers();

    AbstractRoom provideRoom(IServer server);
}
