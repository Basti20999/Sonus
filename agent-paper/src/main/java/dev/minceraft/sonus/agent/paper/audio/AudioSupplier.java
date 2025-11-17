package dev.minceraft.sonus.agent.paper.audio;
// Created by booky10 in TjcSonus (23:39 17.11.2024)

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

@NullMarked
public interface AudioSupplier extends Supplier<short @Nullable []> {
}
