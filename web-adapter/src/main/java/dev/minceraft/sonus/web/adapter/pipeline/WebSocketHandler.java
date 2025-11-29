package dev.minceraft.sonus.web.adapter.pipeline;

import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.minceraft.sonus.web.protocol.packets.WebSocketPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Caught exception in web connection to {} for {}",
                this.connection.getPlayer().getUniqueId(), ctx.channel(), cause);
        ctx.close();
    }
}
