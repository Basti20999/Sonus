package dev.minceraft.sonus.service.commands.arguments;

import dev.minceraft.sonus.service.commands.ArgumentType;
import dev.minceraft.sonus.service.commands.CommandContext;
import org.jspecify.annotations.NullMarked;

import java.util.Locale;

@NullMarked
public class EnumArgument<T extends Enum<T>> implements ArgumentType<T> {

    protected final Class<T> enumClass;
    protected final T[] values;

    protected EnumArgument(Class<T> enumClass) {
        this.enumClass = enumClass;
        this.values = enumClass.getEnumConstants();
    }

    @Override
    public T parse(CommandContext ctx, String input) {
        return Enum.valueOf(this.enumClass, input.toUpperCase(Locale.ROOT));
    }

    @Override
    public StringType getType() {
        return StringType.LITERAL;
    }

    public Class<T> getEnumClass() {
        return this.enumClass;
    }

    public T[] getValues() {
        return this.values;
    }
}
