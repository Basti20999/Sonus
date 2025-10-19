package dev.minceraft.sonus.plasmo.adapter.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.UUID;

@ConfigSerializable
public class PlasmoConfig {

    public UUID serverId = UUID.randomUUID();
}
