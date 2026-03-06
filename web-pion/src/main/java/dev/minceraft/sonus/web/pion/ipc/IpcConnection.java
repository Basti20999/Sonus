package dev.minceraft.sonus.web.pion.ipc;
// Created by booky10 in Sonus (6:16 PM 06.03.2026)

import dev.minceraft.sonus.network.TransportType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnixDomainSocketAddress;
import java.nio.file.Path;

@NullMarked
public final class IpcConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger("PionIpc");

    private final Channel channel;

    private IpcConnection(Channel channel) {
        this.channel = channel;
    }

    public static IpcConnection connect(Path path) {
        TransportType transport = TransportType.get();
        ChannelFuture future = new Bootstrap()
                .channelFactory(transport.getUnixChannelFactory())
                .remoteAddress(UnixDomainSocketAddress.of(path))
                .group(transport.createGroup("sonus-pion-ipc"))
                .connect()
                .awaitUninterruptibly();
        if (!future.isSuccess()) {
            throw new RuntimeException("Error while connecting to unix socket " + path, future.cause());
        }
        LOGGER.info("Connected to unix socket {}", path);
        return new IpcConnection(future.channel());
    }

    public void

    public Channel getChannel() {
        return this.channel;
    }
}
