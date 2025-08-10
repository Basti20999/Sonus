package dev.minceraft.sonus.service.network;
// Created by booky10 in Sonus (01:08 10.08.2025)

import dev.minceraft.sonus.service.SonusConfig;
import dev.minceraft.sonus.service.SonusService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class NetworkManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");
    private static final TransportType TRANSPORT = TransportType.get();

    private final SonusService service;

    private final EventLoopGroup bossGroup = TRANSPORT.createGroup("Boss");
    private @MonotonicNonNull Channel channel;

    public NetworkManager(SonusService service) {
        this.service = service;
    }

    public void bind() {
        SonusConfig config = this.service.getConfig();
        ChannelFuture future = new Bootstrap()
                .group(this.bossGroup)
                .channelFactory(TRANSPORT.getChannelFactory())
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        SonusService service = NetworkManager.this.service;
                        ch.pipeline().addLast(new MagicUdpHandler(service));
                    }
                })
                .bind(config.getBind(), config.getPort());
        CompletableFuture.runAsync(() -> {
            future.awaitUninterruptibly();
            this.channel = future.channel();

            Throwable error = future.cause();
            if (error != null) {
                LOGGER.error("Error while binding sonus udp server", error);
            }
        });
    }

    public @MonotonicNonNull Channel getChannel() {
        return this.channel;
    }
}
