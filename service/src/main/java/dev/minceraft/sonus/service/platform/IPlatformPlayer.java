package dev.minceraft.sonus.service.platform;

import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

@NullMarked
public interface IPlatformPlayer {

    UUID getUniqueId(@Nullable IPlatformPlayer viewer);

    @Nullable
    UUID getServerId();

    String getName(@Nullable IPlatformPlayer viewer);

    void sendPluginMessage(Key key, ByteBuf data);

    void sendBackendPluginMessage(Key key, ByteBuf data);

    void ensureTabListed(IPlatformPlayer target);

    boolean hasPermission(String permission, boolean defaultValue);

    void setPermission(String permission, TriState value);

    boolean canSee(IPlatformPlayer target);

    Locale getLocale();

    Component renderComponent(Component component, Locale locale);

    default Component renderComponent(Component component) {
        return this.renderComponent(component, this.getLocale());
    }

    String renderPlainComponent(Component component, Locale locale);

    default String renderPlainComponent(Component component) {
        return this.renderPlainComponent(component, this.getLocale());
    }

    void sendMessage(Component component);

    default void updateCommands() {
        throw new UnsupportedOperationException();
    }

    boolean isOnline();
}
