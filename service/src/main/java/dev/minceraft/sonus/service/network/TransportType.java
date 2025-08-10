package dev.minceraft.sonus.service.network;
// Created by booky10 in Sonus (01:14 10.08.2025)

import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.incubator.channel.uring.IOUring;
import io.netty.incubator.channel.uring.IOUringDatagramChannel;
import io.netty.incubator.channel.uring.IOUringEventLoopGroup;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@NullMarked
public enum TransportType {

    NIO("nio",
            factory -> new NioEventLoopGroup(0, factory),
            NioDatagramChannel::new
    ),
    EPOLL("epoll",
            factory -> new EpollEventLoopGroup(0, factory),
            EpollDatagramChannel::new
    ),
    IO_URING("io_uring",
            factory -> new IOUringEventLoopGroup(0, factory),
            IOUringDatagramChannel::new
    ),
    KQUEUE("kqueue",
            factory -> new KQueueEventLoopGroup(0, factory),
            KQueueDatagramChannel::new
    ),
    ;

    private static final boolean NO_IO_URING = Boolean.getBoolean("sonus.network.no_io_uring");

    private final String displayName;
    private final Function<ThreadFactory, EventLoopGroup> groupConstructor;
    private final ChannelFactory<? extends DatagramChannel> channelFactory;

    TransportType(
            String displayName,
            Function<ThreadFactory, EventLoopGroup> groupConstructor,
            ChannelFactory<? extends DatagramChannel> channelFactory
    ) {
        this.displayName = displayName;
        this.groupConstructor = groupConstructor;
        this.channelFactory = channelFactory;
    }

    private static ThreadFactory createThreadFactory(String name, String type) {
        String threadName = "Sonus " + name + " " + type + " #";
        AtomicInteger threadNumber = new AtomicInteger();
        return r -> new FastThreadLocalThread(r, threadName + threadNumber.incrementAndGet());
    }

    public static TransportType get() {
        if (!NO_IO_URING && IOUring.isAvailable()) {
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
        return this.groupConstructor.apply(createThreadFactory(this.displayName, type));
    }

    public ChannelFactory<? extends DatagramChannel> getChannelFactory() {
        return this.channelFactory;
    }
}
