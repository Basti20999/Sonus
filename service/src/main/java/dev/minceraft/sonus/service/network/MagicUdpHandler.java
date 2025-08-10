package dev.minceraft.sonus.service.network;
// Created by booky10 in Sonus (01:39 10.08.2025)

import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.adapter.AdapterManager;
import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import org.jspecify.annotations.NullMarked;

import java.net.InetSocketAddress;

@NullMarked
public class MagicUdpHandler extends ChannelInboundHandlerAdapter {

    private final AdapterManager adapters;

    public MagicUdpHandler(SonusService service) {
        this.adapters = service.getAdapters();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof DatagramPacket packet) {
                ByteBuf buf = packet.content();
                if (buf.isReadable()) {
                    this.handle(packet.sender(), buf);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void handle(InetSocketAddress sender, ByteBuf buf) {
        VoiceProtocolAdapter adapter = this.adapters.getAdapter(buf.readByte());
        if (adapter != null) {
            adapter.handleData(sender, buf);
        }
    }
}
