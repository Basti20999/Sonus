package dev.minceraft.sonus.agent.paper.audio;
// Created by booky10 in TjcSonus (22:58 17.11.2024)

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface TickableAudioSupplier extends AudioSupplier {

    void tick();
}
