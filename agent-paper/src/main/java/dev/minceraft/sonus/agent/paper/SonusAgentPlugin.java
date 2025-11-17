package dev.minceraft.sonus.agent.paper;
// Created by booky10 in Sonus (01:39 17.07.2025)

import dev.minceraft.sonus.agent.paper.api.SonusAgentApi;
import dev.minceraft.sonus.agent.paper.api.SonusAgentApiImpl;
import dev.minceraft.sonus.common.config.YamlConfigHolder;
import dev.minceraft.sonus.common.rooms.options.RoomDefinition;
import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import dev.minceraft.sonus.protocol.meta.MetaRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Files;
import java.nio.file.Path;

import static dev.minceraft.sonus.common.SonusConstants.PLUGIN_MESSAGE_CHANNEL;

@NullMarked
public class SonusAgentPlugin extends JavaPlugin {

    private @MonotonicNonNull SonusAgentApiImpl api;
    private @Nullable YamlConfigHolder<RoomDefinition> roomDefinition;

    protected SonusAgentApiImpl createApi() {
        return new SonusAgentApiImpl(this);
    }

    protected AgentListener createAgentListener() {
        return new AgentListener(this);
    }

    protected AgentMessageListener createAgentMessageListener() {
        return new AgentMessageListener(this);
    }

    @Override
    public void onLoad() {
        this.api = this.createApi();
        Bukkit.getServicesManager().register(SonusAgentApi.class, this.api, this, ServicePriority.Normal);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this.createAgentListener(), this);

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, PLUGIN_MESSAGE_CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, PLUGIN_MESSAGE_CHANNEL, this.createAgentMessageListener());

        this.loadRoomDefinition();
    }

    public void sendMetaPacket(IMetaMessage packet) {
        byte[] data = MetaRegistry.write(packet);
        for (Player player : Bukkit.getOnlinePlayers()) {
            // send to first player who has our agent messaging channel registered
            if (player.getListeningPluginChannels().contains(PLUGIN_MESSAGE_CHANNEL)) {
                player.sendPluginMessage(this, PLUGIN_MESSAGE_CHANNEL, data);
                break;
            }
        }
    }

    public void loadRoomDefinition() {
        Path definitionPath = this.getDataPath().resolve("room-definition.yml");
        if (Files.exists(definitionPath)) {
            this.getLogger().info("Loading room definition from file...");
            this.roomDefinition = new YamlConfigHolder<>(RoomDefinition.class, definitionPath);
        }
    }

    public @MonotonicNonNull SonusAgentApiImpl getApi() {
        return this.api;
    }

    public @Nullable RoomDefinition getRoomDefinition() {
        return this.roomDefinition == null ? null : this.roomDefinition.getDelegate();
    }
}
