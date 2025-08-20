package dev.minceraft.sonus.service.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
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
    public void onPlayerSwitch(DisconnectEvent event) {
        this.service.getEventManager().onPlayerSwitchBackend(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onPluginMessageReceived(PluginMessageEvent event) {
        Key channel = ((MinecraftChannelIdentifier) event.getIdentifier()).asKey();
        Player target;
        if (event.getSource() instanceof Player player) {
            target = player;
        } else if (event.getTarget() instanceof Player player) {
            target = player;
        } else {
            throw new IllegalStateException("Plugin message event source or target is not a player");
        }
        SonusPlayer player = this.service.getPlayers().getPlayer(target.getUniqueId());
        if (player == null) {
            LOGGER.info("Received plugin message for player {}({}) but they can't be registered in Sonus", target.getUsername(), target.getUniqueId());
            return;
        }

        if (this.service.getPluginMessenger().handleMessage(channel, player, event.getData())) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
        }
    }
}
