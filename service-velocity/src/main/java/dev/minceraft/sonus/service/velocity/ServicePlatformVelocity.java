package dev.minceraft.sonus.service.velocity;

import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import dev.minceraft.sonus.service.platform.IServer;
import dev.minceraft.sonus.service.platform.IServicePlatform;
import dev.minceraft.sonus.service.platform.ITask;
import dev.minceraft.sonus.service.rooms.AbstractRoom;
import dev.minceraft.sonus.service.rooms.SpatialRoom;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
@NullMarked
public class ServicePlatformVelocity implements IServicePlatform {

    private final ProxyServer server;
    private final Path dataPath;
    private @MonotonicNonNull VelocitySonusService velocityPlugin;

    @Inject
    public ServicePlatformVelocity(ProxyServer server, @DataDirectory Path dataPath) {
        this.server = server;
        this.dataPath = dataPath;
    }

    @Override
    public Path getDataPath() {
        return this.dataPath;
    }

    @Override
    @Nullable
    public IPlatformPlayer getPlayer(UUID uniqueId) {
        return this.server.getPlayer(uniqueId)
                .map(VelocitySonusPlayer::new).orElse(null);
    }

    @Override
    public void registerPluginChannel(Key channel) {
        this.server.getChannelRegistrar().register(MinecraftChannelIdentifier.from(channel));
    }

    @Override
    public ITask executeAsync(Runnable runnable, long period, TimeUnit unit) {
        ScheduledTask schedule = this.server.getScheduler().buildTask(this.velocityPlugin, runnable)
                .repeat(period, unit)
                .schedule();
        return schedule::cancel;
    }

    @Override
    public Set<IServer> getServers() {
        Collection<RegisteredServer> registered = this.server.getAllServers();
        Set<IServer> result = new HashSet<>(registered.size());

        for (RegisteredServer server : registered) {
            result.add(new VelocityServer(server.getServerInfo()));
        }
        return result;
    }

    @Override
    public AbstractRoom provideRoom(IServer server) {
        return new SpatialRoom(server.getUniqueId(), this.velocityPlugin.getService());
    }

    public ServicePlatformVelocity connectPlugin(VelocitySonusService velocityPlugin) {
        this.velocityPlugin = velocityPlugin;
        return this;
    }
}
