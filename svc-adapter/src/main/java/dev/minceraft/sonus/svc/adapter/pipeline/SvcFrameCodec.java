package dev.minceraft.sonus.svc.adapter.pipeline;


import dev.minceraft.sonus.common.protocol.util.ContextMap;
import dev.minceraft.sonus.common.protocol.util.VarInt;
import dev.minceraft.sonus.svc.adapter.SvcUdpPipelineNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.jspecify.annotations.NullMarked;

import java.util.List;

// one of the most useless part's of SVC's protocol... Maybe for lost bytes? But hey then you could also use checksums
@NullMarked
@ChannelHandler.Sharable
public final class SvcFrameCodec extends SvcUdpPipelineNode<ByteBuf, ByteBuf> {

    public static final SvcFrameCodec INSTANCE = new SvcFrameCodec();

    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out, ContextMap ctxMap) {
        out.add(Unpooled.compositeBuffer(2)
                .addComponent(true, VarInt.buffer(msg.readableBytes()))
                .addComponent(true, msg));
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out, ContextMap ctxMap) {
        int size = VarInt.read(msg);
        if (msg.readableBytes() != size) {
            msg.release();
            throw new IllegalStateException("Received invalid readable byte count: " + msg.readableBytes() + " != " + size);
        }
        out.add(msg);
    }
}
