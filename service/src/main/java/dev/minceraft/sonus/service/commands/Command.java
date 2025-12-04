package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (3:40 PM 03.12.2025)

import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public abstract class Command {

    protected final String label;
    protected final List<String> aliases;

    public Command(String label, String... aliases) {
        this.label = label;
        this.aliases = List.of(aliases);
    }

    public abstract LiteralCommandNode construct();

    public void register(CommandHolder commands) {
        LiteralCommandNode node = this.construct();
        commands.register(node);

        for (String alias : this.aliases) {
            LiteralCommandNode aliasNode = new LiteralCommandNode(alias);
            node.copyTo(aliasNode);
            commands.register(aliasNode);
        }
    }

    public String getLabel() {
        return this.label;
    }

    public List<String> getAliases() {
        return this.aliases;
    }
}
