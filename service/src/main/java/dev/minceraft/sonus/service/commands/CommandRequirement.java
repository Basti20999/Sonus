package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (7:53 PM 03.12.2025)

import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface CommandRequirement {

    boolean test(CommandContext ctx) throws CommandException;
}
