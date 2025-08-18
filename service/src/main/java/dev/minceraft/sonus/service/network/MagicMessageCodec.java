package dev.minceraft.sonus.service.network;

import dev.minceraft.sonus.common.protocol.udp.AbstractMagicUdpCodec;
import dev.minceraft.sonus.common.protocol.udp.WrappedUdpPipelineData;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageCodec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
@ChannelHandler.Sharable
public class MagicMessageCodec extends MessageToMessageCodec<DatagramPacket, WrappedUdpPipelineData> {

    private final @Nullable AbstractMagicUdpCodec<?>[] codecs = new AbstractMagicUdpCodec[0xFF + 1];

    @Override
    protected void encode(ChannelHandlerContext ctx, WrappedUdpPipelineData msg, List<Object> out) throws Exception {
        CompositeByteBuf buffer = Unpooled.compositeBuffer(2)
                .addComponent(true, msg.codec().getMagicByteBuf().retainedSlice())
                .addComponent(true, msg.unwrapAndRecycle());
        out.add(new DatagramPacket(buffer, msg.remoteAddress()));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket in, List<Object> out) throws Exception {
        if (!in.content().isReadable()) {
            return;
        }
        short magic = in.content().readUnsignedByte();
        AbstractMagicUdpCodec<?> codec = this.codecs[magic];
        if (codec != null) {
            codec.clearRestMagicByte(in.content());
            out.add(new WrappedUdpPipelineData(codec.getAdapter().newPipelineContext(), in.sender(), codec, in.content().retainedSlice()));
        }
    }

    public void registerCodec(AbstractMagicUdpCodec<?> codec) {
        this.codecs[codec.getMagicByte() & 0xFF] = codec;
    }
}