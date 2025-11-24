package dev.minceraft.sonus.agent.paper;
// Created by booky10 in Sonus (18:48 17.11.2025)

import dev.minceraft.sonus.protocol.meta.IMetaHandler;
import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import dev.minceraft.sonus.protocol.meta.MetaRegistry;
import dev.minceraft.sonus.protocol.meta.agentbound.PlayerConnectionStateMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.papermc.paper.connection.PlayerConnection;
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
        // NO-OP
    }

    @Override
    public void onPluginMessageReceived(String channel, PlayerConnection connection, byte[] message) {
        ByteBuf buf = Unpooled.wrappedBuffer(message);
        IMetaMessage decoded = MetaRegistry.REGISTRY.read(buf);
        if (decoded != null) {
            decoded.handle(this);
        }
    }

    @Override
    public void handlePlayerConnectionState(PlayerConnectionStateMessage message) {
        Set<UUID> players = this.plugin.getApi().getConnectedPlayers();
        if (message.isConnected()) {
            players.add(message.getPlayerId());
        } else {
            players.remove(message.getPlayerId());
        }
    }
}
