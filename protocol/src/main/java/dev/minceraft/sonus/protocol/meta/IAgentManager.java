package dev.minceraft.sonus.protocol.meta;

import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public interface IAgentManager {

    IMetaHandler getAgentListener(UUID serverId);
}
