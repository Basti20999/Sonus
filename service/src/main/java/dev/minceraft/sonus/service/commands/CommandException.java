package dev.minceraft.sonus.service.commands;
// Created by booky10 in Sonus (3:03 PM 03.12.2025)

import org.jspecify.annotations.NullMarked;

@NullMarked
public class CommandException extends RuntimeException {

    public CommandException() {
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // no
    }
}
