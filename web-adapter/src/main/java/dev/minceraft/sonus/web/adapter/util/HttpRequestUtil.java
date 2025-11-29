package dev.minceraft.sonus.web.adapter.util;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
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
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class HttpRequestUtil {

    private static final String API_VERSION_PREFIX = "/api/v";

    private static final String CONNECTION_STATE = "Upgrade";
    private static final String UPGRADE_GOAL = "websocket";

    private HttpRequestUtil() {
    }

    public static ChannelFuture doHttpClose(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        return ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }

    public static ChannelFuture doSocketClose(ChannelHandlerContext ctx, WebSocketCloseStatus status) {
        return doSocketClose(ctx, status, status.reasonText());
    }

    public static ChannelFuture doSocketClose(ChannelHandlerContext ctx, WebSocketCloseStatus status, String reason) {
        WebSocketFrame frame = new CloseWebSocketFrame(status, reason);
        return ctx.writeAndFlush(frame).addListener(ChannelFutureListener.CLOSE);
    }

    public static boolean isWebsocketRequest(HttpRequest request) {
        HttpHeaders headers = request.headers();
        return UPGRADE_GOAL.equals(headers.get(HttpHeaderNames.UPGRADE))
                && headers.get(HttpHeaderNames.CONNECTION, "").contains(CONNECTION_STATE);
    }

    public static int parseApiVersion(String uri) throws HttpErrorException {
        if (!uri.startsWith(API_VERSION_PREFIX)) {
            throw new HttpErrorException(HttpResponseStatus.NOT_FOUND);
        }
        int versionEndIndex = uri.indexOf('/', API_VERSION_PREFIX.length());
        if (versionEndIndex == -1) {
            throw new HttpErrorException(HttpResponseStatus.NOT_FOUND);
        }
        String versionPart = uri.substring(API_VERSION_PREFIX.length(), versionEndIndex);
        try {
            return Integer.parseInt(versionPart);
        } catch (NumberFormatException exception) {
            throw new HttpErrorException(HttpResponseStatus.NOT_FOUND);
        }
    }

    public static String stripApiPrefix(String uri, int version) {
        return uri.substring((API_VERSION_PREFIX + version).length());
    }
}
