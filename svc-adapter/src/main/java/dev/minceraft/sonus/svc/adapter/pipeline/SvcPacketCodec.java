package dev.minceraft.sonus.svc.adapter.pipeline;


import dev.minceraft.sonus.svc.adapter.SvcUdpPipelineNode;
import dev.minceraft.sonus.svc.protocol.registries.SvcVoicePacketRegistry;
import dev.minceraft.sonus.svc.protocol.voice.SvcVoicePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

@ChannelHandler.Sharable
public class SvcPacketCodec extends SvcUdpPipelineNode<ByteBuf, SvcVoicePacket<?>> {

    public static final SvcPacketCodec INSTANCE = new SvcPacketCodec();

    @Override
    public void encode(ChannelHandlerContext ctx, SvcVoicePacket<?> msg, List<Object> out, SvcUdpContext svcCtx) throws Exception {
        ByteBuf buf = ctx.alloc().buffer();
        SvcVoicePacketRegistry.REGISTRY.write(buf, msg);
        out.add(buf);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out, SvcUdpContext svcCtx) throws Exception {
        SvcVoicePacket<?> packet = SvcVoicePacketRegistry.REGISTRY.read(msg);
        if (packet != null) {
            out.add(packet);
        }
    }
}
