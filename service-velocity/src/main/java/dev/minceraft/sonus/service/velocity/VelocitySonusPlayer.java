package dev.minceraft.sonus.service.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class VelocitySonusPlayer implements IPlatformPlayer {

    private final Player player;

    public VelocitySonusPlayer(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return this.player.getUniqueId();
    }

    @Override
    public String getName() {
        return this.player.getUsername();
    }

    @Override
    public void sendPluginMessage(Key key, ByteBuf data) {
        this.player.sendPluginMessage(MinecraftChannelIdentifier.from(key), data.array());
    }
}
