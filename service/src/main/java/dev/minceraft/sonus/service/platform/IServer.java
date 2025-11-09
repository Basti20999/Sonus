package dev.minceraft.sonus.service.platform;

import dev.minceraft.sonus.common.rooms.options.RoomDefinition;

import java.util.UUID;

public interface IServer {

    UUID getUniqueId();

    String getName();
}
