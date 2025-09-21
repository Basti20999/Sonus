package dev.minceraft.sonus.service.platform;

import java.util.UUID;

public interface IServer {

    UUID getUniqueId();

    String getName();
}
