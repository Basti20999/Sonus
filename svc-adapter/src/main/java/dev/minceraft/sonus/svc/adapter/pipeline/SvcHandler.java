package dev.minceraft.sonus.svc.adapter.pipeline;

import dev.minceraft.sonus.common.protocol.udp.AbstractUdpPipelineHandler;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import dev.minceraft.sonus.svc.protocol.SvcUdpMagicCodec;
import dev.minceraft.sonus.svc.protocol.voice.SvcVoicePacket;
import io.netty.channel.ChannelHandlerContext;

public class SvcHandler extends AbstractUdpPipelineHandler<SvcVoicePacket<?>, SvcUdpContext> {

    public SvcHandler(SvcUdpMagicCodec svcCodec) {
        super(svcCodec);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, SvcVoicePacket<?> msg, SvcUdpContext svcCtx) {
        SvcConnection connection = svcCtx.connection;
        if (connection == null) {
            throw new IllegalStateException("Try to handle a message without a connection set in the context!");
        }
        connection.setRemoteAddress(svcCtx.remoteAddress);
        msg.handle(connection.getVoiceHandler());
    }
}
