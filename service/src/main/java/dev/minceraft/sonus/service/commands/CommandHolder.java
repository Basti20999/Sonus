package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (3:40 PM 03.12.2025)

import dev.minceraft.sonus.service.SonusService;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NullMarked
public final class CommandHolder {

    private final SonusService service;
    private final Map<String, LiteralCommandNode> nodes = new HashMap<>();

    public CommandHolder(SonusService service) {
        this.service = service;
    }

    public void dispatch(CommandSender sender, String label, List<String> args) throws CommandException {
        LiteralCommandNode node = this.nodes.get(label);
        if (node == null) {
            throw new CommandException("Can't find command with label " + label);
        }
        CommandContext ctx = new CommandContext(this.service, sender);
        if (!node.parseAndExecute(ctx, new ArrayDeque<>(args))) {
            throw new CommandException("Can't execute " + label + " with args " + args);
        }
    }

    public void register(LiteralCommandNode node) {
        if (this.nodes.putIfAbsent(node.getName(), node) != null) {
            throw new IllegalStateException("Command with name " + node.getName() + " is already registered");
        }
    }

    public void iterateNodes(Consumer<LiteralCommandNode> node) {
        this.nodes.values().forEach(node);
    }
}
