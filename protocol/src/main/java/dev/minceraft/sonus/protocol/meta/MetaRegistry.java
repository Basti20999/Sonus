package dev.minceraft.sonus.protocol.meta;
// Created by booky10 in Sonus (01:56 17.07.2025)

import dev.minceraft.sonus.protocol.meta.servicebound.PlayerPositionsMessage;
import dev.minceraft.sonus.protocol.registry.ProtocolRegistry;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MetaRegistry {

    public static final ProtocolRegistry<IMetaMessage> REGISTRY = new ProtocolRegistry.Builder<IMetaMessage>()
            .register(PlayerPositionsMessage.class,PlayerPositionsMessage::new)
            .build();

    private MetaRegistry() {
    }
}
