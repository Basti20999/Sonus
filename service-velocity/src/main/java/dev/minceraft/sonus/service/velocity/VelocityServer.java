package dev.minceraft.sonus.service.velocity;

import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.minceraft.sonus.service.platform.IServer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class VelocityServer implements IServer {

    private final ServerInfo server;
    private final UUID uniqueId;

    public VelocityServer(ServerInfo server) {
        this.server = server;
        this.uniqueId = UUID.nameUUIDFromBytes(server.getName().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public String getName() {
        return this.server.getName();
    }
}
