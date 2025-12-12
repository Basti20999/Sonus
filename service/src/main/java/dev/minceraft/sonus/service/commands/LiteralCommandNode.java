package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (2:58 PM 03.12.2025)

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class LiteralCommandNode extends CommandNode {

    LiteralCommandNode(String name) {
        super(name);
    }

    @Override
    public void parse(CommandContext ctx, @Nullable String arg) throws CommandException {
        // NO-OP
    }

    @Override
    public LiteralCommandNode executes(CommandExecutor executor) {
        super.executes(executor);
        return this;
    }

    @Override
    public LiteralCommandNode requires(CommandRequirement requirement) {
        super.requires(requirement);
        return this;
    }

    @Override
    public LiteralCommandNode with(CommandNode node) {
        super.with(node);
        return this;
    }
}
