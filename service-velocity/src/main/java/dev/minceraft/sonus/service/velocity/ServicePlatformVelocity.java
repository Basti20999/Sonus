package dev.minceraft.sonus.service.velocity;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import dev.minceraft.sonus.service.platform.IServer;
import dev.minceraft.sonus.service.platform.IServicePlatform;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@NullMarked
@Singleton
public class ServicePlatformVelocity implements IServicePlatform {

    private final ProxyServer server;
    private final Path dataPath;

    private final LoadingCache<UUID, @Nullable IServer> serverCache;

    @Inject
    public ServicePlatformVelocity(ProxyServer server, @DataDirectory Path dataPath) {
        this.server = server;
        this.dataPath = dataPath;

        this.serverCache = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(serverId -> {
                    for (RegisteredServer velServer : server.getAllServers()) {
                        if (serverId.equals(VelocityServer.generateUniqueId(velServer.getServerInfo()))) {
                            return new VelocityServer(velServer.getServerInfo());
                        }
                    }
                    return null;
                });
    }

    @Override
    public Path getDataPath() {
        return this.dataPath;
    }

    public IPlatformPlayer getPlayer(Player player) {
        return new VelocitySonusPlayer(this.server, player);
    }

    @Override
    public @Nullable IPlatformPlayer getPlayer(UUID uniqueId) {
        return this.server.getPlayer(uniqueId)
                .map(this::getPlayer)
                .orElse(null);
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
        IServer server = this.serverCache.get(uniqueId);
        if (server == null) {
            throw new IllegalStateException("Can't find server with id " + uniqueId);
        }
        return server;
    }
}
