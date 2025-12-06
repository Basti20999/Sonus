package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (3:40 PM 03.12.2025)

import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public final class CommandHolder {

    private final Map<String, LiteralCommandNode> nodes = new HashMap<>();

    public void register(LiteralCommandNode node) {
        if (this.nodes.putIfAbsent(node.getName(), node) != null) {
            throw new IllegalStateException("Command with name " + node.getName() + " is already registered");
        }
    }

    public List<LiteralCommandNode> getNodes() {
        return List.copyOf(this.nodes.values());
    }
}
