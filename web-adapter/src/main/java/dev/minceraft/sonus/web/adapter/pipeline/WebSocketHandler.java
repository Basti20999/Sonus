package dev.minceraft.sonus.web.adapter.pipeline;

import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.minceraft.sonus.web.protocol.packets.WebSocketPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class WebSocketHandler extends ChannelInboundHandlerAdapter {

    private final WebSocketConnection connection;

    public WebSocketHandler(WebSocketConnection connection) {
        this.connection = connection;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof WebSocketPacket packet) {
                packet.handle(this.connection.getWebSocketHandler());
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
