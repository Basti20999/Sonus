package dev.minceraft.sonus.svc.adapter.pipeline;

import dev.minceraft.sonus.svc.adapter.SvcUdpPipelineNode;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

@ChannelHandler.Sharable
public class SvcCipherCodec extends SvcUdpPipelineNode<ByteBuf, ByteBuf> {

    public static final SvcCipherCodec INSTANCE = new SvcCipherCodec();

    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out, SvcUdpContext adapterCtx) throws Exception {
        SvcConnection connection = adapterCtx.connection;
        if (connection == null) {
            msg.release();
            throw new IllegalStateException("Try to encrypt a message without a connection set in the context!");
        }
        connection.getCipher().encode(ctx, msg, out, adapterCtx);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out, SvcUdpContext adapterCtx) throws Exception {
        SvcConnection connection = adapterCtx.connection;
        if (connection == null) {
            msg.release();
            throw new IllegalStateException("Try to decrypt a message without a connection set in the context!");
        }
        connection.getCipher().decode(ctx, msg, out, adapterCtx);
    }
}
