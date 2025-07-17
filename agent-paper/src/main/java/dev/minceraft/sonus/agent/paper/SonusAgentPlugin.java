package dev.minceraft.sonus.agent.paper;
// Created by booky10 in Sonus (01:39 17.07.2025)

import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import dev.minceraft.sonus.protocol.meta.MetaRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import static dev.minceraft.sonus.protocol.util.SonusConstants.CURRENT_VERSION;
import static dev.minceraft.sonus.protocol.util.SonusConstants.PLUGIN_MESSAGE_CHANNEL;

@NullMarked
public class SonusAgentPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new MovementListener(this), this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, PLUGIN_MESSAGE_CHANNEL);
    }

    public void sendMetaPacket(IMetaMessage packet) {
        byte[] data = MetaRegistry.REGISTRY.writeAsArray(packet, CURRENT_VERSION);
        Bukkit.getServer().sendPluginMessage(this, PLUGIN_MESSAGE_CHANNEL, data);
    }
}
