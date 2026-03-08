package dev.minceraft.sonus.web.pion.ipc.pipeline;
// Created by booky10 in Sonus (9:27 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.ipc.IpcMessage;
import dev.minceraft.sonus.web.pion.ipc.IpcMessageRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
@ChannelHandler.Sharable
public class IpcMessageCodec extends MessageToMessageCodec<ByteBuf, IpcMessage> {

    public static final IpcMessageCodec CODEC = new IpcMessageCodec();

    private IpcMessageCodec() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, IpcMessage msg, List<Object> out) {
        ByteBuf buf = ctx.alloc().buffer();
        try {
            IpcMessageRegistry.REGISTRY.encode(buf, msg);
            out.add(buf.retain());
        } finally {
            buf.release();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        out.add(IpcMessageRegistry.REGISTRY.decode(msg));
    }
}
