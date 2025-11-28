package dev.minceraft.sonus.web.adapter.pipeline;

import dev.minceraft.sonus.web.adapter.WsAdapter;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketHandshaker extends ChannelInboundHandlerAdapter {

    private static final String CONNECTION_STATE = "Upgrade";
    private static final String UPGRADE_GOAL = "websocket";

    private final WsAdapter wsAdapter;
    private State state = State.INITIAL;

    public WebSocketHandshaker(WsAdapter wsAdapter) {
        this.wsAdapter = wsAdapter;
    }

    private ChannelFuture doHttpClose(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        return ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }

    private ChannelFuture doSocketClose(ChannelHandlerContext ctx, WebSocketCloseStatus status) {
        return this.doSocketClose(ctx, status, status.reasonText());
    }

    private ChannelFuture doSocketClose(ChannelHandlerContext ctx, WebSocketCloseStatus status, String reason) {
        WebSocketFrame frame = new CloseWebSocketFrame(status, reason);
        return ctx.writeAndFlush(frame).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (this.state) {
            case INITIAL -> {
                if (!(msg instanceof HttpRequest request)) {
                    this.doHttpClose(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    return;
                }
                HttpHeaders headers = request.headers();
                if (!UPGRADE_GOAL.equals(headers.get(HttpHeaderNames.UPGRADE))
                        || !headers.get(HttpHeaderNames.CONNECTION, "").contains(CONNECTION_STATE)) {
                    this.doHttpClose(ctx, HttpResponseStatus.BAD_REQUEST);
                    return;
                }
                System.out.println(request.uri());
            }
        }
    }

    private enum State {
        INITIAL,
        AWAITING_AUTH,
        FINISHED
    }
}
