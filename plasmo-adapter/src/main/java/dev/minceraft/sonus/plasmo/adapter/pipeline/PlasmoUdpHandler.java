package dev.minceraft.sonus.plasmo.adapter.pipeline;

import dev.minceraft.sonus.common.protocol.udp.AbstractMagicUdpCodec;
import dev.minceraft.sonus.common.protocol.udp.AbstractUdpPipelineHandler;
import dev.minceraft.sonus.plasmo.protocol.udp.UdpPlasmoPacket;
import io.netty.channel.ChannelHandlerContext;

public class PlasmoUdpHandler extends AbstractUdpPipelineHandler<UdpPlasmoPacket<?>, PlasmoUdpContext> {

    public PlasmoUdpHandler(AbstractMagicUdpCodec<?> codec) {
        super(codec);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, UdpPlasmoPacket<?> msg, PlasmoUdpContext adapterCtx) {
        msg.handle(adapterCtx.connection.getVoiceHandler());
    }
}
