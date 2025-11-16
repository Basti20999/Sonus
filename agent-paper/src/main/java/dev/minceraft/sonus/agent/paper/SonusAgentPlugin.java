package dev.minceraft.sonus.agent.paper;
// Created by booky10 in Sonus (01:39 17.07.2025)

import dev.minceraft.sonus.common.config.YamlConfigHolder;
import dev.minceraft.sonus.common.rooms.options.RoomDefinition;
import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import dev.minceraft.sonus.protocol.meta.MetaRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Files;
import java.nio.file.Path;

import static dev.minceraft.sonus.common.SonusConstants.PLUGIN_MESSAGE_CHANNEL;

@NullMarked
public class SonusAgentPlugin extends JavaPlugin {

    private @Nullable YamlConfigHolder<RoomDefinition> roomDefinition;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new AgentListener(this), this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, PLUGIN_MESSAGE_CHANNEL);

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
        if (!Files.exists(definitionPath)) {
            return; // Ignore if not present
        }
        this.getLogger().info("Loading room definition...");
        this.roomDefinition = new YamlConfigHolder<>(RoomDefinition.class, definitionPath);
    }

    @Nullable
    public RoomDefinition getRoomDefinition() {
        if (this.roomDefinition == null) {
            return null;
        }
        return this.roomDefinition.getDelegate();
    }
}
