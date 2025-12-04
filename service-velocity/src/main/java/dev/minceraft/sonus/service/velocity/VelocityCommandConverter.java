package dev.minceraft.sonus.service.velocity;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.commands.CommandContext;
import dev.minceraft.sonus.service.commands.CommandNode;
import dev.minceraft.sonus.service.commands.CommandSender;
import dev.minceraft.sonus.service.commands.LiteralCommandNode;
import net.kyori.adventure.text.Component;

public class VelocityCommandConverter {

    public static BrigadierCommand convertLiteralCommandNode(SonusService service, LiteralCommandNode node) {
        LiteralArgumentBuilder<CommandSource> builder = BrigadierCommand.literalArgumentBuilder(node.getName());
        builder.requires(source ->
                node.checkRequirement(new CommandContext(service, convertCommandSource(service, source))));
        builder.executes(ctx ->
                service.getCommandHolder().dispatch(convertCommandSource(service, ctx.getSource()), node.getName(), ctx.get))
    }

    private static void appendCommandChildren(LiteralArgumentBuilder<CommandSource> builder, CommandNode node) {

    }

    private static CommandSender convertCommandSource(SonusService service, CommandSource commandSource) {
        if (commandSource instanceof Player player) {
            return service.getPlayerManager().getPlayer(player.getUniqueId());
        } else if (commandSource instanceof ConsoleCommandSource console) {
            return new ConsoleCommandSender(console);
        }
        throw new IllegalArgumentException("Unsupported CommandSource type: " + commandSource.getClass().getName());
    }

    private record ConsoleCommandSender(ConsoleCommandSource source) implements CommandSender {
        @Override
        public boolean hasPermission(String permission, boolean defaultValue) {
            Tristate permissionValue = this.source.getPermissionValue(permission);
            if (permissionValue != Tristate.UNDEFINED) {
                return permissionValue.asBoolean();
            }
            return defaultValue;
        }

        @Override
        public void sendMessage(Component component) {
            this.source.sendMessage(component);
        }
    }
}
