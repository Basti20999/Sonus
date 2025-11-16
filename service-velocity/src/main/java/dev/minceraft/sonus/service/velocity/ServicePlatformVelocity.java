package dev.minceraft.sonus.service.velocity;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.minceraft.sonus.common.rooms.options.RoomDefinition;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import dev.minceraft.sonus.service.platform.IServer;
import dev.minceraft.sonus.service.platform.IServicePlatform;
import dev.minceraft.sonus.service.rooms.AbstractRoom;
import dev.minceraft.sonus.service.rooms.ServerRoom;
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
    private final LoadingCache<UUID, IServer> serverCache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public @Nullable IServer load(UUID uuid) throws Exception {
                    for (IServer iServer : ServicePlatformVelocity.this.getServers()) {
                        if (iServer.getUniqueId().equals(uuid)) {
                            return iServer;
                        }
                    }
                    return null;
                }
            });
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
                .map(player -> new VelocitySonusPlayer(this.server, player)).orElse(null);
    }

    @Override
    public void registerPluginChannel(Key channel) {
        this.server.getChannelRegistrar().register(MinecraftChannelIdentifier.from(channel));
    }

    @Override
    public Set<IServer> getServers() {
        Collection<RegisteredServer> registered = this.server.getAllServers();
        ImmutableSet.Builder<IServer> result = ImmutableSet.builderWithExpectedSize(registered.size());
        for (RegisteredServer server : registered) {
            result.add(new VelocityServer(server.getServerInfo()));
        }
        return result.build();
    }

    @Override
    public IServer getServer(UUID uniqueId) {
        return this.serverCache.get(uniqueId);
    }

    @Override
    public AbstractRoom provideRoom(IServer server) {
        return new ServerRoom(this.velocityPlugin.getService(), server, new RoomDefinition());
    }

    public ServicePlatformVelocity connectPlugin(VelocitySonusService velocityPlugin) {
        this.velocityPlugin = velocityPlugin;
        return this;
    }
}
