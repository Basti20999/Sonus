package dev.minceraft.sonus.common;
// Created by booky10 in Sonus (02:23 17.07.2025)

import dev.minceraft.sonus.common.data.WorldVec3d;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public interface IAudioSource {

    UUID getSenderId();

    default @Nullable WorldVec3d getPosition() {
        return null;
    }

    default @Nullable String getCategory() {
        return null;
    }
}
