package dev.minceraft.sonus.svc.adapter.pipeline;

import dev.minceraft.sonus.common.protocol.util.ContextMap;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.svc.adapter.SvcUdpPipelineNode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.UUID;

@NullMarked
@ChannelHandler.Sharable
public class SvcPlayerMarkerCodec extends SvcUdpPipelineNode<ByteBuf, ByteBuf> {

    public static final SvcPlayerMarkerCodec INSTANCE = new SvcPlayerMarkerCodec();

    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out, ContextMap ctxMap) {
        out.add(msg);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out, ContextMap ctxMap) {
        UUID playerId = DataTypeUtil.readUniqueId(msg);
        ctxMap.put("playerId", playerId);
        out.add(msg.retainedSlice());
    }
}
