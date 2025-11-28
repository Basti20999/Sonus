package dev.minceraft.sonus.network;
// Created by booky10 in Sonus (01:14 10.08.2025)

import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.uring.IoUring;
import io.netty.channel.uring.IoUringDatagramChannel;
import io.netty.channel.uring.IoUringIoHandler;
import io.netty.channel.uring.IoUringServerSocketChannel;
import io.netty.channel.uring.IoUringSocketChannel;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@NullMarked
public enum TransportType {

    NIO("nio", NioIoHandler::newFactory, NioServerSocketChannel::new, NioSocketChannel::new, NioDatagramChannel::new),
    EPOLL("epoll", EpollIoHandler::newFactory, EpollServerSocketChannel::new, EpollSocketChannel::new, EpollDatagramChannel::new),
    IO_URING("io_uring", IoUringIoHandler::newFactory, IoUringServerSocketChannel::new, IoUringSocketChannel::new, IoUringDatagramChannel::new),
    KQUEUE("kqueue", KQueueIoHandler::newFactory, KQueueServerSocketChannel::new, KQueueSocketChannel::new, KQueueDatagramChannel::new),
    ;

    private static final boolean NO_IO_URING = Boolean.getBoolean("sonus.network.no_io_uring");

    private final String displayName;
    private final Supplier<IoHandlerFactory> ioHandlerCtor;

    // tcp
    private final ChannelFactory<? extends ServerSocketChannel> serverSocketChannelFactory;
    private final ChannelFactory<? extends SocketChannel> socketChannelFactory;

    // udp
    private final ChannelFactory<? extends DatagramChannel> datagramChannelFactory;

    TransportType(
            String displayName,
            Supplier<IoHandlerFactory> ioHandlerCtor,
            ChannelFactory<? extends ServerSocketChannel> serverSocketChannelFactory,
            ChannelFactory<? extends SocketChannel> socketChannelFactory,
            ChannelFactory<? extends DatagramChannel> datagramChannelFactory
    ) {
        this.displayName = displayName;
        this.ioHandlerCtor = ioHandlerCtor;
        this.serverSocketChannelFactory = serverSocketChannelFactory;
        this.socketChannelFactory = socketChannelFactory;
        this.datagramChannelFactory = datagramChannelFactory;
    }

    private static ThreadFactory createThreadFactory(String name, String type) {
        String threadName = "Sonus " + name + " " + type + " #";
        AtomicInteger threadNumber = new AtomicInteger();
        return r -> new FastThreadLocalThread(r, threadName + threadNumber.incrementAndGet());
    }

    public static TransportType get() {
        if (!NO_IO_URING && IoUring.isAvailable()) {
            return IO_URING;
        } else if (Epoll.isAvailable()) {
            return EPOLL;
        } else if (KQueue.isAvailable()) {
            return KQUEUE;
        } else {
            return NIO;
        }
    }

    public EventLoopGroup createGroup(String type) {
        ThreadFactory threadFactory = createThreadFactory(this.displayName, type);
        return new MultiThreadIoEventLoopGroup(0, threadFactory, this.ioHandlerCtor.get());
    }

    public ChannelFactory<? extends ServerSocketChannel> getServerSocketChannelFactory() {
        return this.serverSocketChannelFactory;
    }

    public ChannelFactory<? extends SocketChannel> getSocketChannelFactory() {
        return this.socketChannelFactory;
    }

    public ChannelFactory<? extends DatagramChannel> getDatagramChannelFactory() {
        return this.datagramChannelFactory;
    }
}
