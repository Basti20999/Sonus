package dev.minceraft.sonus.service.commands;

import net.kyori.adventure.text.Component;

public interface CommandSender {

    boolean hasPermission(String permission, boolean defaultValue);

    default boolean hasPermission(String permission) {
        return hasPermission(permission, false);
    }

    void sendMessage(Component component);
}
