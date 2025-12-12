package dev.minceraft.sonus.svc.adapter.config;

import dev.minceraft.sonus.common.config.ISubConfig;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class SvcConfig implements ISubConfig {

    public boolean enabled = true;
}
