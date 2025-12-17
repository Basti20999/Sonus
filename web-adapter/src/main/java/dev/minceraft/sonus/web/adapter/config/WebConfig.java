package dev.minceraft.sonus.web.adapter.config;

import dev.minceraft.sonus.common.config.ISubConfig;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.net.InetSocketAddress;

@ConfigSerializable
public class WebConfig implements ISubConfig {

    public boolean enabled = true;

    public InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8032);

    public String linkPattern = "https://sonus.example.com/%s";
}
