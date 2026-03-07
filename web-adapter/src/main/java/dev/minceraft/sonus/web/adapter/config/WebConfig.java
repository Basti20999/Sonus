package dev.minceraft.sonus.web.adapter.config;

import dev.minceraft.sonus.common.config.ISubConfig;
import dev.minceraft.sonus.web.pion.ipc.model.BundlePolicy;
import dev.minceraft.sonus.web.pion.ipc.model.IceServer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.net.InetSocketAddress;
import java.util.List;

@ConfigSerializable
public class WebConfig implements ISubConfig {

    public boolean enabled = true;

    public boolean useRootCommand = true;

    public InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8032);

    public String linkPattern = "https://sonus.example.com/%s";

    public List<IceServerConfig> iceServers = List.of(
            new IceServerConfig("stun:stun.l.google.com:5349", null, null)
    );

    @ConfigSerializable
    public record IceServerConfig(
            String url, @Nullable String user, @Nullable String auth
    ) {

        public IceServer create() {
            return new IceServer(this.url, this.user, this.auth);
        }
    }

    public BundlePolicy bundlePolicy = BundlePolicy.MAX_BUNDLE;
}
