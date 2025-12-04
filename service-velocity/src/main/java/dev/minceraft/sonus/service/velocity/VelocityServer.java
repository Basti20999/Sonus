package dev.minceraft.sonus.service.velocity;

import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.minceraft.sonus.service.platform.IServer;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@NullMarked
public class VelocityServer implements IServer {

    private final ServerInfo server;
    private final UUID uniqueId;

    public VelocityServer(ServerInfo server) {
        this.server = server;
        this.uniqueId = generateUniqueId(server);
    }

    public static UUID generateUniqueId(ServerInfo server) {
        return UUID.nameUUIDFromBytes(("VeloServer:" + server.getName()).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public Component getName() {
        return Component.text(this.server.getName());
    }

    @Override
    public @Nullable String getType() {
        return null; // not applicable
    }
}
