package dev.minceraft.sonus.service.network;

import dev.minceraft.sonus.common.protocol.udp.AbstractMagicUdpCodec;
import dev.minceraft.sonus.common.protocol.udp.UdpBasedContext;
import dev.minceraft.sonus.common.protocol.udp.WrappedUdpPipelineData;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageCodec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

@NullMarked
@ChannelHandler.Sharable
public class MagicMessageCodec extends MessageToMessageCodec<DatagramPacket, WrappedUdpPipelineData> {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final @Nullable AbstractMagicUdpCodec<?>[] codecs = new AbstractMagicUdpCodec[0xFF + 1];

    @Override
    protected void encode(ChannelHandlerContext ctx, WrappedUdpPipelineData msg, List<Object> out) throws Exception {
        InetSocketAddress recipient = msg.context().remoteAddress;
        CompositeByteBuf buffer = Unpooled.compositeBuffer(2)
                .addComponent(true, msg.codec().getMagicByteBuf().retainedSlice())
                .addComponent(true, msg.unwrapAndRecycle());

        out.add(new DatagramPacket(buffer, recipient));
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
            UdpBasedContext<?> context = codec.getAdapter().newPipelineContext();
            context.remoteAddress = in.sender();
            out.add(new WrappedUdpPipelineData(context, codec, in.content().retainedSlice()));
        }
    }

    public void registerCodec(AbstractMagicUdpCodec<?> codec) {
        if (this.codecs[codec.getMagicByte() & 0xFF] != null) {
            LOGGER.warn("Overriding existing codec for magic byte {}", codec.getMagicByte());
        }
        this.codecs[codec.getMagicByte() & 0xFF] = codec;
        LOGGER.info("Registered codec {}", codec.getClass().getSimpleName());
    }
}