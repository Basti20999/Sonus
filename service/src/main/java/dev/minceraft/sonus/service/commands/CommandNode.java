package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (2:58 PM 03.12.2025)

import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.player.SonusPlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

@NullMarked
public abstract class CommandNode {

    private static final Pattern ARG_SPLIT_PATTERN = Pattern.compile("\\s+");

    protected final String name;
    protected @Nullable CommandExecutor executor;
    protected final List<CommandNode> children = new ArrayList<>();

    protected CommandNode(String name) {
        this.name = name;
    }

    public static LiteralCommandNode literal(String name) {
        return new LiteralCommandNode(name);
    }

    public static <T> ArgumentCommandNode<T> argument(String name, ArgumentType<T> argumentType) {
        return new ArgumentCommandNode<T>(name, argumentType);
    }

    protected boolean execute(CommandContext ctx) throws CommandException {
        if (this.executor != null) {
            return this.executor.execute(ctx);
        }
        return false; // no executor, fail
    }

    protected abstract boolean parseAndExecuteThis(CommandContext ctx, Queue<String> args) throws CommandException;

    public final boolean parseAndExecute(SonusService service, SonusPlayer player, String args) throws CommandException {
        String[] argsSplit = ARG_SPLIT_PATTERN.split(args.trim());
        Queue<String> argsQueue = new ArrayDeque<>(argsSplit.length);
        argsQueue.addAll(List.of(argsSplit));

        CommandContext ctx = new CommandContext(service, player);
        return this.parseAndExecute(ctx, argsQueue);
    }

    protected final boolean parseAndExecute(CommandContext ctx, Queue<String> args) throws CommandException {
        // parse this command node
        this.parseAndExecuteThis(ctx, args);

        if (this.children.isEmpty()) {
            return this.execute(ctx); // final node, execute!
        } else if (this.children.size() == 1) {
            return this.children.getFirst().parseAndExecute(ctx, args);
        }
        // try to match first valid
        for (CommandNode child : this.children) {
            if (child.parseAndExecute(ctx, new ArrayDeque<>(args))) {
                return true;
            }
        }
        return false;
    }

    public CommandNode execute(CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    public CommandNode with(CommandNode node) {
        this.children.add(node);
        return this;
    }

    public String getName() {
        return this.name;
    }
}
