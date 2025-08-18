package dev.minceraft.sonus.common.data;

import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public interface ISonusPlayer {

    UUID getUniqueId();

    String getName();
}
