package dev.minceraft.sonus.service.agent;
// Created by booky10 in Sonus (02:29 16.11.2025)

import dev.minceraft.sonus.common.protocol.tcp.IPluginMessageSource;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public abstract class PluginMessageSourceImpl implements IPluginMessageSource {

    protected final IPlatformPlayer player;

    PluginMessageSourceImpl(IPlatformPlayer player) {
        this.player = player;
    }

    @Override
    public UUID getPlayerId() {
        return this.player.getUniqueId();
    }

    @Override
    public @Nullable UUID getServerId() {
        return this.player.getServerId();
    }

    public IPlatformPlayer getPlayer() {
        return this.player;
    }

    public static class Player extends PluginMessageSourceImpl implements IPluginMessageSource.Player {

        public Player(IPlatformPlayer player) {
            super(player);
        }
    }

    public static class Server extends PluginMessageSourceImpl implements IPluginMessageSource.Server {

        public Server(IPlatformPlayer player) {
            super(player);
        }
    }
}
