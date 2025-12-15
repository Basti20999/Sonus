package dev.minceraft.sonus.service.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.minceraft.sonus.service.SonusService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Plugin(
        id = "sonus",
        name = "Sonus",
        version = "1.0.0",
        authors = {"booky10", "pianoman911"},
        url = "https://minceraft.dev/"
)
@Singleton
public class VelocitySonusService {

    private final SonusService service;
    private final ProxyServer server;
    private final Provider<SonusTranslationLoader> i18n;

    @Inject
    public VelocitySonusService(
            ProxyServer server,
            ServicePlatformVelocity platform,
            Provider<SonusTranslationLoader> i18n
    ) {
        this.service = new SonusService(platform);
        this.server = server;
        this.i18n = i18n;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        this.i18n.get().loadAndRegister();

        this.service.getScheduler().execute(() -> {
            this.service.init();
            this.server.getEventManager().register(this, new VelocityListener(this.service));
        });
    }

    public SonusService getService() {
        return this.service;
    }
}
