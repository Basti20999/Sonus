package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (3:40 PM 03.12.2025)

import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.player.SonusPlayer;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public final class CommandManager {

    private final SonusService service;
    private final Map<String, LiteralCommandNode> nodes = new HashMap<>();

    public CommandManager(SonusService service) {
        this.service = service;
    }

    public void dispatch(SonusPlayer player, String label, List<String> args) throws CommandException {
        LiteralCommandNode node = this.nodes.get(label);
        if (node == null) {
            throw new CommandException("Can't find command with label " + label);
        }
        CommandContext ctx = new CommandContext(this.service, player);
        if (!node.parseAndExecute(ctx, new ArrayDeque<>(args))) {
            throw new CommandException("Can't execute " + label + " with args " + args);
        }
    }

    public void register(LiteralCommandNode node) {
        if (this.nodes.putIfAbsent(node.getName(), node) != null) {
            throw new IllegalStateException("Command with name " + node.getName() + " is already registered");
        }
    }
}
