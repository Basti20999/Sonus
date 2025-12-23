package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (3:03 PM 03.12.2025)

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class CommandException extends RuntimeException {

    private @Nullable Component component;

    public CommandException(Component component) {
        this.component = component;
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandException(Component component, Throwable cause) {
        super(cause);
        this.component = component;
    }

    @Nullable
    public Component getComponent() {
        return this.component;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // no
    }
}
