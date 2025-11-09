package dev.minceraft.sonus.protocol.meta;

import java.util.UUID;

public interface IAgentManager {

    IMetaHandler getAgentListener(UUID serverId);
}
