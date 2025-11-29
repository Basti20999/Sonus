package dev.minceraft.sonus.service.platform;

import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public interface IServer {

    UUID getUniqueId();

    String getName();
}
