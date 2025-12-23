package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (2:58 PM 03.12.2025)

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NullMarked
public sealed abstract class CommandNode permits ArgumentCommandNode, LiteralCommandNode {

    protected final String name;
    protected @Nullable CommandExecutor executor;
    protected @Nullable CommandRequirement requirement;
    protected final List<CommandNode> children = new ArrayList<>();

    protected CommandNode(String name) {
        this.name = name;
    }

    public static LiteralCommandNode literal(String name) {
        return new LiteralCommandNode(name);
    }

    public static <T> ArgumentCommandNode<T> argument(String name, ArgumentType<T> argumentType) {
        return new ArgumentCommandNode<>(name, argumentType);
    }

    public boolean hasExecutor() {
        return this.executor != null;
    }

    @Nullable
    public CommandExecutor getExecutor() {
        return this.executor;
    }

    public boolean execute(CommandContext ctx) throws CommandException {
        if (this.executor != null) {
            return this.executor.execute(ctx);
        }
        return false; // no executor, fail
    }

    public boolean hasRequirement() {
        return this.requirement != null;
    }

    public boolean checkRequirement(CommandContext ctx) throws CommandException {
        if (this.requirement != null) {
            return this.requirement.test(ctx);
        }
        return true; // no requirement, pass
    }

    public abstract void parse(CommandContext ctx, @Nullable String arg) throws CommandException;

    public CommandNode executes(CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    public CommandNode requires(CommandRequirement requirement) {
        this.requirement = requirement;
        return this;
    }

    public CommandNode with(CommandNode node) {
        this.children.add(node);
        return this;
    }

    public void copyTo(CommandNode target) {
        target.executor = this.executor;
        target.requirement = this.requirement;
        target.children.addAll(this.children);
    }

    public String getName() {
        return this.name;
    }

    public List<CommandNode> getChildren() {
        return Collections.unmodifiableList(this.children);
    }
}
