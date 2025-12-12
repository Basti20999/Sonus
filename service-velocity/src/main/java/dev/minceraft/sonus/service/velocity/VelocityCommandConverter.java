package dev.minceraft.sonus.service.velocity;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.commands.ArgumentCommandNode;
import dev.minceraft.sonus.service.commands.CommandContext;
import dev.minceraft.sonus.service.commands.CommandNode;
import dev.minceraft.sonus.service.commands.CommandSender;
import dev.minceraft.sonus.service.commands.LiteralCommandNode;
import dev.minceraft.sonus.service.player.SonusPlayer;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

import java.util.LinkedList;
import java.util.List;

@NullMarked
public final class VelocityCommandConverter {

    private final SonusService service;
    private final Object plugin;
    private final CommandManager commands;

    public VelocityCommandConverter(SonusService service, Object plugin, CommandManager commands) {
        this.service = service;
        this.plugin = plugin;
        this.commands = commands;
    }

    public void registerCommand(LiteralCommandNode rootNode) {
        CommandMeta meta = this.commands.metaBuilder(rootNode.getName()).plugin(this.plugin).build();
        this.commands.register(meta, this.convertRootNode(rootNode));
    }

    public BrigadierCommand convertRootNode(LiteralCommandNode node) {
        LiteralArgumentBuilder<CommandSource> builder = BrigadierCommand.literalArgumentBuilder(node.getName());

        List<CommandNode> stack = new LinkedList<>();
        stack.add(node);
        this.convertNode(stack, builder);

        return new BrigadierCommand(builder);
    }

    private void convertNode(List<CommandNode> nodes, ArgumentBuilder<CommandSource, ?> builder) {
        CommandNode node = nodes.getLast();
        if (node.hasRequirement()) {
            builder.requires(source -> node.checkRequirement(this.createContext(source)));
        }
        if (node.hasExecutor()) {
            builder.executes(ctx -> {
                CommandContext sctx = this.createContext(ctx.getSource());
                // iterate through ancestor nodes and extract all arguemnts
                for (CommandNode ancestor : nodes) {
                    if (ancestor instanceof ArgumentCommandNode<?>) {
                        String argVal = ctx.getArgument(ancestor.getName(), String.class);
                        ancestor.parse(sctx, argVal);
                    }
                }
                // execute node with all the provided context
                return node.execute(sctx) ? Command.SINGLE_SUCCESS : 0;
            });
        }
        // recursively convert all children
        for (CommandNode child : node.getChildren()) {
            nodes.addLast(child);
            this.convertNode(nodes, builder);
            nodes.removeLast();
        }
    }

    private CommandContext createContext(CommandSource source) {
        return new CommandContext(this.service, this.convertCommandSource(source));
    }

    private CommandSender convertCommandSource(CommandSource source) {
        if (source instanceof Player player) {
            SonusPlayer sonusPlayer = this.service.getPlayerManager().getPlayer(player.getUniqueId());
            if (sonusPlayer == null) {
                // players are dynamically created
                throw new IllegalStateException("Can't get sonus player instance for " + player);
            }
            return sonusPlayer;
        } else if (source instanceof ConsoleCommandSource console) {
            return new ConsoleCommandSender(console);
        }
        throw new IllegalArgumentException("Unsupported CommandSource type: " + source.getClass().getName());
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
