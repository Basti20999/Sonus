package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (2:59 PM 03.12.2025)

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ArgumentCommandNode<T> extends CommandNode {

    private final ArgumentType<T> type;

    ArgumentCommandNode(String name, ArgumentType<T> type) {
        super(name);
        this.type = type;
    }

    @Override
    public void parse(CommandContext ctx, @Nullable String arg) throws CommandException {
        if (arg == null) {
            throw new CommandException("Missing argument");
        } else if (!this.checkRequirement(ctx)) {
            throw new CommandException("Requirement failed");
        }
        ctx.set(this, this.type.parse(ctx, arg));
    }

    public ArgumentType<T> getType() {
        return this.type;
    }

    @Override
    public ArgumentCommandNode<T> executes(CommandExecutor executor) {
        super.executes(executor);
        return this;
    }

    @Override
    public ArgumentCommandNode<T> requires(CommandRequirement requirement) {
        super.requires(requirement);
        return this;
    }

    @Override
    public ArgumentCommandNode<T> with(CommandNode node) {
        super.with(node);
        return this;
    }
}
