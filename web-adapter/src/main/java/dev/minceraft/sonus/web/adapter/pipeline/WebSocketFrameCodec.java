package dev.minceraft.sonus.web.adapter.pipeline;

import dev.minceraft.sonus.web.adapter.messages.AbstractWebSocketMessage;
import dev.minceraft.sonus.web.adapter.messages.ByteWebSocketMessage;
import dev.minceraft.sonus.web.adapter.messages.TextWebSocketMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

public class WebSocketFrameCodec extends MessageToMessageCodec<WebSocketFrame, AbstractWebSocketMessage> {

    private ByteBuf buf;
    private StringBuilder builder;

    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractWebSocketMessage msg, List<Object> out) {
        if (msg instanceof TextWebSocketMessage txt) {
            out.add(new TextWebSocketFrame(txt.getText()));
        } else if (msg instanceof ByteWebSocketMessage binary) {
            out.add(new BinaryWebSocketFrame(binary.getDirectBuf().retain()));
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) {
        if (msg instanceof TextWebSocketFrame txt) {
            this.builder.append(txt.text());
            if (msg.isFinalFragment()) {
                out.add(new TextWebSocketMessage(this.builder.toString()));
                this.builder = new StringBuilder();
            }
        } else if (msg instanceof BinaryWebSocketFrame binary) {
            this.buf.writeBytes(binary.content());
            if (msg.isFinalFragment()) {
                out.add(new ByteWebSocketMessage(this.buf.copy()));
                this.buf.clear();
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.buf = ctx.alloc().buffer();
        this.builder = new StringBuilder();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        this.buf.release();
        this.builder = null;
    }
}
