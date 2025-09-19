package dev.minceraft.sonus.service.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import dev.minceraft.sonus.common.protocol.tcp.MessageSource;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.player.SonusPlayer;
import net.kyori.adventure.key.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VelocityListener {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final SonusService service;

    public VelocityListener(SonusService service) {
        this.service = service;
    }

    @Subscribe
    public void onPlayerSwitch(ServerConnectedEvent event) {
        this.service.getEventManager().onPlayerSwitchBackend(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onQuit(DisconnectEvent event) {
        this.service.getEventManager().onPlayerQuit(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onPluginMessageReceived(PluginMessageEvent event) {
        Key channel = ((MinecraftChannelIdentifier) event.getIdentifier()).asKey();
        Player target;
        MessageSource source;
        if (event.getSource() instanceof Player player) {
            target = player;
            source = MessageSource.PLAYER;
        } else if (event.getTarget() instanceof Player player) {
            target = player;
            source = MessageSource.SERVER;
        } else {
            throw new IllegalStateException("Plugin message event source or target is not a player");
        }
        SonusPlayer player = this.service.getPlayerManager().getPlayer(target.getUniqueId());
        if (player == null) {
            LOGGER.info("Received plugin message for player {}({}) but they can't be registered in Sonus", target.getUsername(), target.getUniqueId());
            return;
        }

        if (this.service.getPluginMessenger().handleMessage(channel, source, player, event.getData())) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
        }
    }
}
