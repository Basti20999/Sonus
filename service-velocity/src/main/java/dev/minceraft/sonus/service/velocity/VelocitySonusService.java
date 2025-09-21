package dev.minceraft.sonus.service.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.minceraft.sonus.service.SonusService;
import jakarta.inject.Inject;

@Plugin(
        id = "sonus-velocity",
        name = "Sonus",
        version = "1.0.0",
        authors = {"booky10", "pianoman911"},
        url = "https://minceraft.dev/"
)
public class VelocitySonusService {

    private final SonusService service;
    private final ProxyServer server;
    private final ServicePlatformVelocity platform;

    @Inject
    public VelocitySonusService(ProxyServer server, ServicePlatformVelocity platform) {
        this.platform = platform.connectPlugin(this);
        this.service = new SonusService(platform);
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        this.service.init();
        this.server.getEventManager().register(this, new VelocityListener(this.service));
    }

    public SonusService getService() {
        return this.service;
    }
}
