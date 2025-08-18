package dev.minceraft.sonus.service.velocity;

import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import dev.minceraft.sonus.service.platform.IServicePlatform;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;

@Singleton
@NullMarked
public class ServicePlatformVelocity implements IServicePlatform {

    private final ProxyServer server;
    private final Path configPath;

    @Inject
    public ServicePlatformVelocity(ProxyServer server, @DataDirectory Path dataPath) {
        this.server = server;
        this.configPath = dataPath.resolve("config.yml");
    }

    @Override
    public Path getConfigPath() {
        return this.configPath;
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
}
