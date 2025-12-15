package dev.minceraft.sonus.service.commands.arguments;

import dev.minceraft.sonus.service.commands.ArgumentType;
import dev.minceraft.sonus.service.commands.CommandContext;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class StringArgument implements ArgumentType<String> {

    public static final StringArgument INSTANCE = new StringArgument();

    private StringArgument() {
    }

    @Override
    public String parse(CommandContext ctx, String input) {
        return input;
    }
}
