package dev.minceraft.sonus.web.adapter.config;

import dev.minceraft.sonus.common.config.ISubConfig;
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

//        public RTCIceServer create() {
//            RTCIceServer ret = new RTCIceServer();
//            ret.urls.add(this.url);
//            ret.username = this.user;
//            ret.password = this.auth;
//            return ret;
//        }
    }

    public RtcNetworkConfig rtcNetwork = new RtcNetworkConfig();

    @ConfigSerializable
    public static final class RtcNetworkConfig {

        public int minPort = 49100;
        public int maxPort = 49300;
        public boolean enableTcp = false;
        public boolean enableUdp = true;
        public boolean enableIpv6 = true;
        public boolean enableStun = true;
        public boolean enableRelay = true;
    }
}
