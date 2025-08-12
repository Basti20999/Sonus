package dev.minceraft.sonus.svc.adapter.pipeline;

import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.protocol.udp.AbstractUdpPipelineHandler;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import dev.minceraft.sonus.svc.protocol.SvcUdpMagicCodec;
import dev.minceraft.sonus.svc.protocol.voice.SvcVoicePacket;
import io.netty.channel.ChannelHandlerContext;

public class SvcHandler extends AbstractUdpPipelineHandler<SvcVoicePacket<?>, SvcUdpContext> {

    private final ISonusService service;

    public SvcHandler(ISonusService service) {
        super(SvcUdpMagicCodec.INSTANCE);
        this.service = service;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, SvcVoicePacket<?> msg, SvcUdpContext svcCtx) {
        SvcConnection connection = svcCtx.connection;
        if (connection == null) {
            throw new IllegalStateException("Try to handle a message without a connection set in the context!");
        }
        msg.handle(connection.getVoiceHandler());
    }
}
