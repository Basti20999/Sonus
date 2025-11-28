package dev.minceraft.sonus.web.adapter;

import dev.minceraft.sonus.network.TransportType;
import dev.minceraft.sonus.web.adapter.pipeline.WebSocketHandshaker;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class WebServer {

    public static final String HTTP_CODEC = "http-codec";
    public static final String HTTP_AGGREGATOR = "http-aggregator";
    public static final String HTTP_SOCKET_SHAKER = "http-socket-shaker";

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private static final TransportType TRANSPORT = TransportType.get();
    private static final int MAX_MSG_LENGTH = 1 << 16; // 64 KiB

    private final WsAdapter adapter;

    private @MonotonicNonNull EventLoopGroup bossGroup;
    private @MonotonicNonNull EventLoopGroup workerGroup;

    private @MonotonicNonNull Channel channel;

    public WebServer(WsAdapter adapter) {
        this.adapter = adapter;
    }

    public void openSocket() {
        this.adapter.getService().getConfigHolder().addReloadHookAndRun(config -> {
            LOGGER.info("Starting WebServer...");
            this.shutdown();

            this.bossGroup = TRANSPORT.createGroup("boss");
            this.workerGroup = TRANSPORT.createGroup("worker");

            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(this.bossGroup, this.workerGroup)
                    .channelFactory(TRANSPORT.getServerSocketChannelFactory())
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(HTTP_CODEC, new HttpServerCodec())
                                    .addLast(HTTP_AGGREGATOR, new HttpObjectAggregator(MAX_MSG_LENGTH))
                                    .addLast(HTTP_SOCKET_SHAKER, new WebSocketHandshaker(WebServer.this.adapter));
                        }
                    });
            InetSocketAddress address = config.getWebConfig().getAddress();
            try {
                this.channel = bootstrap.bind(address).sync().channel();
                LOGGER.info("WebServer started on address {}", address);
            } catch (InterruptedException exception) {
                throw new RuntimeException("Failed to bind to " + address, exception);
            }
        });
    }

    public void shutdown() {
        if (this.channel != null) {
            this.channel.close().syncUninterruptibly();
        }
        if (this.bossGroup != null) {
            this.bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }
}
