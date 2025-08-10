package dev.minceraft.sonus.common.protocol.udp;

import io.netty.channel.ChannelHandler;

import java.net.InetSocketAddress;

public interface IUdpServer {

    <T> void registerCodec(AbstractMagicUdpCodec<T> codec);

    void registerHandler(String name, ChannelHandler handler);

    InetSocketAddress getHostAddress();

    InetSocketAddress getRemoteAddress();

}
