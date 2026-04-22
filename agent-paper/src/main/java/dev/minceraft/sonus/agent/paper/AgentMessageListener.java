package dev.minceraft.sonus.agent.paper;
// Created by booky10 in Sonus (18:48 17.11.2025)

import dev.minceraft.sonus.agent.paper.events.VoiceConnectionStateChangeEvent;
import dev.minceraft.sonus.protocol.meta.IMetaHandler;
import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import dev.minceraft.sonus.protocol.meta.MetaRegistry;
import dev.minceraft.sonus.protocol.meta.agentbound.PlayerConnectionStateMessage;
import dev.minceraft.sonus.protocol.meta.agentbound.PlayerVoicePingMessage;
import dev.minceraft.sonus.protocol.meta.agentbound.TriggerCommandUpdateMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.papermc.paper.connection.PlayerConnection;
import io.papermc.paper.connection.PlayerGameConnection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.UUID;

@NullMarked
public class AgentMessageListener implements PluginMessageListener, IMetaHandler {

    private final SonusAgentPlugin plugin;

    public AgentMessageListener(SonusAgentPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // legacy behavior to support versions before the new configuration api
        this.handle(message);
    }

    @Override
    public void onPluginMessageReceived(String channel, PlayerConnection connection, byte[] message) {
        // don't duplicate handling
        if (!(connection instanceof PlayerGameConnection)) {
            this.handle(message);
        }
    }

    private void handle(byte[] message) {
        ByteBuf buf = Unpooled.wrappedBuffer(message);
        IMetaMessage decoded = MetaRegistry.REGISTRY.decode(buf);
        if (decoded != null) {
            decoded.handle(this);
        }
    }

    @Override
    public void handlePlayerConnectionState(PlayerConnectionStateMessage message) {
        Player player = Bukkit.getPlayer(message.getPlayerId());
        Set<UUID> players = this.plugin.getApi().getConnectedPlayers();
        if (message.isVoiceActive()) {
            // add to set
            if (players.add(message.getPlayerId()) && player != null) {
                new VoiceConnectionStateChangeEvent(player, true).callEvent();
            }
        } else {
            // remove from set
            if (players.remove(message.getPlayerId()) && player != null) {
                new VoiceConnectionStateChangeEvent(player, false).callEvent();
            }
        }
    }

    @Override
    public void handleTriggerCommandUpdate(TriggerCommandUpdateMessage message) {
        // some service implementations may not be able to update
        // commands and instead delegate this to the agent implementation
        Player player = Bukkit.getPlayer(message.getPlayerId());
        if (player != null) {
            player.updateCommands();
        }
    }

    @Override
    public void handlePlayerVoicePing(PlayerVoicePingMessage message) {
        this.plugin.getApi().setVoicePing(message.getPlayerId(), message.getPingMs());
    }
}
