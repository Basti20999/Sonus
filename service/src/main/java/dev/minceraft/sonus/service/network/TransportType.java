package dev.minceraft.sonus.service.network;
// Created by booky10 in Sonus (01:14 10.08.2025)

import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.uring.IoUring;
import io.netty.channel.uring.IoUringDatagramChannel;
import io.netty.channel.uring.IoUringIoHandler;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@NullMarked
public enum TransportType {

    NIO("nio", NioIoHandler::newFactory, NioDatagramChannel::new),
    EPOLL("epoll", EpollIoHandler::newFactory, EpollDatagramChannel::new),
    IO_URING("io_uring", IoUringIoHandler::newFactory, IoUringDatagramChannel::new),
    KQUEUE("kqueue", KQueueIoHandler::newFactory, KQueueDatagramChannel::new),
    ;

    private static final boolean NO_IO_URING = Boolean.getBoolean("sonus.network.no_io_uring");

    private final String displayName;
    private final Supplier<IoHandlerFactory> ioHandlerCtor;
    private final ChannelFactory<? extends DatagramChannel> channelFactory;

    TransportType(
            String displayName,
            Supplier<IoHandlerFactory> ioHandlerCtor,
            ChannelFactory<? extends DatagramChannel> channelFactory
    ) {
        this.displayName = displayName;
        this.ioHandlerCtor = ioHandlerCtor;
        this.channelFactory = channelFactory;
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

    public ChannelFactory<? extends DatagramChannel> getChannelFactory() {
        return this.channelFactory;
    }
}
