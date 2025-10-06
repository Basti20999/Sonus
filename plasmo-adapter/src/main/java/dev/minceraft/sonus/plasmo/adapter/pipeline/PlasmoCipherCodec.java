package dev.minceraft.sonus.plasmo.adapter.pipeline;

import dev.minceraft.sonus.plasmo.adapter.PlasmoUdpPipelineNode;
import dev.minceraft.sonus.plasmo.protocol.PlasmoUdpMagicCodec;
import dev.minceraft.sonus.plasmo.protocol.cipher.IEncryptable;
import dev.minceraft.sonus.plasmo.protocol.udp.UdpPlasmoPacket;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public class PlasmoCipherCodec extends PlasmoUdpPipelineNode<UdpPlasmoPacket<?>, UdpPlasmoPacket<?>> {

    public PlasmoCipherCodec(PlasmoUdpMagicCodec plasmoCodec) {
        super(plasmoCodec);
    }

    @Override
    public void encode(ChannelHandlerContext ctx, UdpPlasmoPacket<?> msg, List<Object> out, PlasmoUdpContext adapterCtx) throws Exception {
        if (msg instanceof IEncryptable encryptable) {
            encryptable.encrypt(adapterCtx.connection.getCipher());
        }

        out.add(msg);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, UdpPlasmoPacket<?> msg, List<Object> out, PlasmoUdpContext adapterCtx) throws Exception {
        if (msg instanceof IEncryptable encryptable) {
            encryptable.decrypt(adapterCtx.connection.getCipher());
        }

        out.add(msg);
    }
}
