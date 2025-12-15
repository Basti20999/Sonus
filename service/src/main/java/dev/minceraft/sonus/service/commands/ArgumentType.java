package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (2:59 PM 03.12.2025)

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ArgumentType<T> {

    T parse(CommandContext ctx, String input);

    default StringType getType() {
        return StringType.STRING;
    }

    enum StringType {
        WORD,
        STRING,
        GREEDY,
        /**
         * Only allowed to be used in {@link dev.minceraft.sonus.service.commands.arguments.EnumArgument}
         */
        @ApiStatus.Internal
        LITERAL,
    }
}
