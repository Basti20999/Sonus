package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (2:59 PM 03.12.2025)

import dev.minceraft.sonus.service.SonusService;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@NullMarked
public class CommandContext {

    private final SonusService service;
    private final CommandSender sender;
    private final Map<String, ArgumentValue<?>> arguments = new HashMap<>();

    public CommandContext(SonusService service, CommandSender sender) {
        this.service = service;
        this.sender = sender;
    }

    public <T> void set(ArgumentCommandNode<T> node, T value) {
        if (this.arguments.putIfAbsent(node.getName(), new ArgumentValue<>(node, value)) != null) {
            throw new IllegalStateException("Duplicate command argument with name " + node.getName());
        }
    }

    public <T> T get(String name, ArgumentType<T> type) throws CommandException {
        ArgumentValue<?> val = this.arguments.get(name);
        if (val == null) {
            throw new CommandException("Failed to provide argument " + name);
        } else if (!Objects.equals(val.type(), type)) {
            throw new CommandException("Argument type mismatch for " + name + ": " + val.type() + " != " + type);
        }
        @SuppressWarnings("unchecked") // checked
        T castVal = (T) val.value();
        return castVal;
    }

    public <T> Optional<T> getOr(String name, ArgumentType<T> type) throws CommandException {
        ArgumentValue<?> val = this.arguments.get(name);
        if (val == null || !Objects.equals(val.type(), type)) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked") // checked
        T castVal = (T) val.value();
        return Optional.ofNullable(castVal);
    }

    public SonusService service() {
        return this.service;
    }

    public CommandSender sender() {
        return this.sender;
    }

    protected record ArgumentValue<T>(ArgumentType<T> type, @UnknownNullability T value) {

        protected ArgumentValue(ArgumentCommandNode<T> type, @UnknownNullability T value) {
            this(type.getType(), value);
        }
    }
}
