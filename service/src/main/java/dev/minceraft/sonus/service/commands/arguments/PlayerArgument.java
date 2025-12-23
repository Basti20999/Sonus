package dev.minceraft.sonus.service.commands.arguments;

import dev.minceraft.sonus.service.commands.ArgumentType;
import dev.minceraft.sonus.service.commands.CommandContext;
import dev.minceraft.sonus.service.commands.CommandException;
import dev.minceraft.sonus.service.player.SonusPlayer;
import org.jspecify.annotations.NullMarked;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@NullMarked
public class PlayerArgument implements ArgumentType<SonusPlayer> {

    public static final PlayerArgument INSTANCE = new PlayerArgument();

    @Override
    public SonusPlayer parse(CommandContext ctx, String input) {
        SonusPlayer sender = (SonusPlayer) ctx.sender();
        for (SonusPlayer player : ctx.service().getPlayerManager().getPlayers()) {
            if (player.isConnected() && player.getName(sender).equalsIgnoreCase(input)) {
                return player;
            }
        }
        throw new CommandException(translatable("sonus.command.argument.player.not_found", text(input)));
    }
}
