package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (3:19 PM 03.12.2025)

import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface CommandExecutor {

    boolean execute(CommandContext ctx) throws CommandException;
}
