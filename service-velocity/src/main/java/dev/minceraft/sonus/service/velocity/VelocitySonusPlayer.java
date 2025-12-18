package dev.minceraft.sonus.service.velocity;

import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import dev.minceraft.sonus.service.platform.IPlatformPlayer;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

@NullMarked
public class VelocitySonusPlayer implements IPlatformPlayer {

    private final ProxyServer server;
    private final Player player;

    public VelocitySonusPlayer(ProxyServer server, Player player) {
        this.server = server;
        this.player = player;
    }

    @Override
    public UUID getUniqueId(@Nullable IPlatformPlayer viewer) {
        return this.player.getUniqueId();
    }

    @Override
    @Nullable
    public UUID getServerId() {
        return this.player.getCurrentServer().map(server ->
                new VelocityServer(server.getServerInfo()).getUniqueId()).orElse(null);
    }

    @Override
    public String getName(@Nullable IPlatformPlayer viewer) {
        return this.player.getUsername();
    }

    @Override
    public void sendPluginMessage(Key key, ByteBuf data) {
        try {
            byte[] array = new byte[data.readableBytes()];
            data.readBytes(array);
            this.player.sendPluginMessage(MinecraftChannelIdentifier.from(key), array);
        } finally {
            data.release();
        }
    }

    @Override
    public void sendBackendPluginMessage(Key key, ByteBuf data) {
        try {
            if (this.player.isActive()) {
                this.player.getCurrentServer().ifPresent(server -> {
                    byte[] array = new byte[data.readableBytes()];
                    data.readBytes(array);
                    server.sendPluginMessage(MinecraftChannelIdentifier.from(key), array);
                });
            }
        } finally {
            data.release();
        }
    }

    @Override
    public void ensureTabListed(IPlatformPlayer target) {
        TabList tabList = this.player.getTabList();
        if (tabList.getEntry(target.getUniqueId(this)).isPresent()) {
            return; // Already present
        }
        Optional<Player> targetPlayer = this.server.getPlayer(target.getUniqueId(this));
        if (targetPlayer.isEmpty()) {
            return; // Target player not online
        }
        tabList.addEntry(TabListEntry.builder()
                .profile(targetPlayer.get().getGameProfile())
                .listed(false) // We don't want them to be visible in the tab list
                .showHat(true) // Force showing hats
                .tabList(tabList)
                .build());
    }

    @Override
    public boolean hasPermission(String permission, boolean defaultValue) {
        Tristate permissionValue = this.player.getPermissionValue(permission);
        if (permissionValue != Tristate.UNDEFINED) {
            return permissionValue.asBoolean();
        }
        return defaultValue;
    }

    @Override
    public boolean canSeeFallback(IPlatformPlayer target) {
        // if the source and target are on the same server, this fallback method
        // should only be called if there is no state present yet during login, so hide the target there
        return this.server != ((VelocitySonusPlayer) target).server;
    }

    @Override
    public Locale getLocale() {
        Locale locale = this.player.getEffectiveLocale();
        if (locale == null && this.player.hasSentPlayerSettings()) {
            locale = this.player.getPlayerSettings().getLocale();
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    // why doesn't velocity expose their internal method for this?
    @Override
    public Component renderComponent(Component component, Locale locale) {
        return GlobalTranslator.render(component, locale);
    }

    @Override
    public String renderPlainComponent(Component component, Locale locale) {
        Component rendered = this.renderComponent(component, locale);
        return plainText().serialize(rendered);
    }

    @Override
    public void sendMessage(Component component) {
        this.player.sendMessage(component);
    }

    @Override
    public boolean isOnline() {
        return this.player.isActive();
    }
}
