package dev.minceraft.sonus.plasmo.adapter.pipeline;

import dev.minceraft.sonus.plasmo.adapter.PlasmoAdapter;
import dev.minceraft.sonus.plasmo.adapter.PlasmoUdpPipelineNode;
import dev.minceraft.sonus.plasmo.protocol.PlasmoUdpMagicCodec;
import dev.minceraft.sonus.plasmo.protocol.udp.UdpPlasmoPacket;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public class PlasmoPlayerMarkerCodec extends PlasmoUdpPipelineNode<UdpPlasmoPacket<?>, UdpPlasmoPacket<?>> {

    private final PlasmoAdapter adapter;

    public PlasmoPlayerMarkerCodec(PlasmoUdpMagicCodec plasmoCodec, PlasmoAdapter adapter) {
        super(plasmoCodec);
        this.adapter = adapter;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, UdpPlasmoPacket<?> msg, List<Object> out, PlasmoUdpContext adapterCtx) throws Exception {
        out.add(msg);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, UdpPlasmoPacket<?> msg, List<Object> out, PlasmoUdpContext adapterCtx) throws Exception {
        adapterCtx.connection = this.adapter.getSessionManager().getConnectionBySecret(msg.getSecret());
        if (adapterCtx.connection == null) {
            return;
        }
        adapterCtx.connection.setRemoteAddress(adapterCtx.remoteAddress);
        out.add(msg);
    }
}
