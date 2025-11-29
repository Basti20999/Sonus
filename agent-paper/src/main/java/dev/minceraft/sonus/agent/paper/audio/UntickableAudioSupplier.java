package dev.minceraft.sonus.agent.paper.audio;
// Created by booky10 in Sonus (20:33 24.11.2025)

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class UntickableAudioSupplier implements AudioSupplier {

    private final AudioSupplier delegate;

    public UntickableAudioSupplier(AudioSupplier delegate) {
        this.delegate = delegate;
    }

    @Override
    public short @Nullable [] getAndTick() {
        return this.delegate.get(0);
    }

    @Override
    public void tick() {
        // NO-OP
    }

    @Override
    public short @Nullable [] get() {
        return this.delegate.get();
    }

    @Override
    public short @Nullable [] get(int offset) {
        return this.delegate.get(offset);
    }

    public AudioSupplier getDelegate() {
        return this.delegate;
    }
}
