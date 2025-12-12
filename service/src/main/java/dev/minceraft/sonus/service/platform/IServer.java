package dev.minceraft.sonus.service.platform;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public interface IServer {

    UUID getUniqueId();

    Component getName();

    @Nullable String getType();
}
