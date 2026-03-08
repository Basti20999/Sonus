package dev.minceraft.sonus.web.pion.ipc;
// Created by booky10 in Sonus (6:16 PM 06.03.2026)

import dev.minceraft.sonus.network.TransportType;
import dev.minceraft.sonus.web.pion.ipc.pionbound.IpcPeerClose;
import dev.minceraft.sonus.web.pion.ipc.pipeline.FrameDecoder;
import dev.minceraft.sonus.web.pion.ipc.pipeline.FrameEncoder;
import dev.minceraft.sonus.web.pion.ipc.pipeline.IpcMessageCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.ReferenceCountUtil;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@NullMarked
public final class IpcConnection implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger("PionIpc");

    private final Channel channel;

    private final Map<Integer, IpcHandler> handlers = new ConcurrentHashMap<>();
    private final AtomicInteger handlerCount = new AtomicInteger();

    private final ChannelInboundHandler handler = new ChannelInboundHandlerAdapter() {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                if (msg instanceof IpcMessage ipcMsg) {
                    IpcHandler handler = IpcConnection.this.handlers.get(ipcMsg.handlerId);
                    if (handler == null) {
                        LOGGER.warn("Received IPC message for unknown handler {}: {}", ipcMsg.handlerId, ipcMsg);
                    } else {
                        ipcMsg.handle(handler);
                    }
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOGGER.error("Received exception from IPC connection", cause);
        }
    };

    private IpcConnection(Channel channel) {
        this.channel = channel;
    }

    public static IpcConnection connect(Path path) {
        CompletableFuture<IpcConnection> future = new CompletableFuture<>();
        TransportType transport = TransportType.get();
        // connect to socket
        ChannelFuture channelFuture = new Bootstrap()
                .channelFactory(transport.getUnixChannelFactory())
                .remoteAddress(new DomainSocketAddress(path.toAbsolutePath().toString()))
                .group(transport.createGroup("sonus-pion-ipc"))
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        // setup ipc message pipeline
                        IpcConnection connection = new IpcConnection(ch);
                        ch.pipeline()
                                .addLast(new FrameDecoder())
                                .addLast(FrameEncoder.ENCODER)
                                .addLast(IpcMessageCodec.CODEC)
                                .addLast(connection.handler);
                        future.complete(connection);
                    }
                })
                .connect()
                .awaitUninterruptibly();
        if (!channelFuture.isSuccess()) {
            throw new RuntimeException("Error while connecting to unix socket " + path, channelFuture.cause());
        }
        // wait for channel to be fully initialized
        IpcConnection ret;
        try {
            ret = future.get(3L, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException exception) {
            throw new RuntimeException("Timed out while waiting for ipc connection for " + path, exception);
        }
        LOGGER.info("Connected to unix socket {}", path);
        return ret;
    }

    public int registerHandler(IpcHandler handler) {
        int id = this.handlerCount.getAndIncrement();
        this.handlers.put(id, handler);
        return id;
    }

    public void unregisterHandler(int handlerId) {
        this.handlers.remove(handlerId);
    }

    public void send(IpcMessage message) {
        // skip sending messages for unregistered handlers
        if (this.handlers.containsKey(message.getHandlerId())) {
            this.channel.writeAndFlush(message);
        }
    }

    @Override
    public void close() {
        if (this.channel.isActive()) {
            // safely shutdown
            CompletableFuture.runAsync(() -> {
                this.handlers.keySet().removeIf(handlerId -> {
                    this.channel.write(new IpcPeerClose(handlerId));
                    return true;
                });
                this.channel.flush();
                this.channel.close();
            }, this.channel.eventLoop()).join();
        } else {
            // abort, channel is already closed
            this.channel.close();
            this.handlers.clear();
        }
    }
}
