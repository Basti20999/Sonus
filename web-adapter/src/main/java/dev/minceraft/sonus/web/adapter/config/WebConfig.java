package dev.minceraft.sonus.web.adapter.config;

import dev.minceraft.sonus.common.config.ISonusWebConfig;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.net.InetSocketAddress;

@ConfigSerializable
public class WebConfig implements ISonusWebConfig {

    private InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8032);

    @Override
    public InetSocketAddress getAddress() {
        return this.address;
    }
}
