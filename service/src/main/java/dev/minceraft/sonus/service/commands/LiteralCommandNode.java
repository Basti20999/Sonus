package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (2:58 PM 03.12.2025)

import org.jspecify.annotations.NullMarked;

import java.util.Queue;

@NullMarked
public class LiteralCommandNode extends CommandNode {

    protected LiteralCommandNode(String name) {
        super(name);
    }

    @Override
    protected boolean parseAndExecuteThis(CommandContext ctx, Queue<String> args) throws CommandException {
        return true; // NO-OP
    }
}
