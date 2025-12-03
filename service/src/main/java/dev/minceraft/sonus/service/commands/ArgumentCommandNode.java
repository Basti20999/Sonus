package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (2:59 PM 03.12.2025)

import org.jspecify.annotations.NullMarked;

import java.util.Queue;

@NullMarked
public final class ArgumentCommandNode<T> extends CommandNode {

    private final ArgumentType<T> type;

    ArgumentCommandNode(String name, ArgumentType<T> type) {
        super(name);
        this.type = type;
    }

    @Override
    protected boolean parseAndExecuteThis(CommandContext ctx, Queue<String> args) throws CommandException {
        try {
            ctx.set(this, this.type.parse(ctx, args.remove()));
            return true; // valid argument node
        } catch (CommandException ignored) {
            return false; // failed to parse
        }
    }

    public ArgumentType<T> getType() {
        return this.type;
    }

    @Override
    public ArgumentCommandNode<T> execute(CommandExecutor executor) {
        super.execute(executor);
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
