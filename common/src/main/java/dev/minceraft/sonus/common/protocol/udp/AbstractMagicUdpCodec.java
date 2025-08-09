package dev.minceraft.sonus.common.protocol.udp;

import dev.minceraft.sonus.common.protocol.util.ContextMap;
import dev.minceraft.sonus.common.protocol.util.TypeUtil;
import io.leangen.geantyref.TypeToken;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ChannelHandler.Sharable
public abstract class AbstractMagicUdpCodec<T> extends MessageToMessageCodec<DatagramPacket, WrappedUdpPipelineData> {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final byte magicByte;
    private final ByteBuf magicByteBuf;
    private final Class<T> packetClass;

    public AbstractMagicUdpCodec(byte magicByte, Class<T> packetClass) {
        this.magicByte = magicByte;
        this.magicByteBuf = Unpooled.wrappedBuffer(new byte[]{magicByte});
        this.packetClass = packetClass;
    }

    public AbstractMagicUdpCodec(byte magicByte, TypeToken<T> packetTypeToken) {
        this.magicByte = magicByte;
        this.magicByteBuf = Unpooled.wrappedBuffer(new byte[]{magicByte});
        this.packetClass = TypeUtil.resolveType(packetTypeToken);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof AbstractMagicUdpCodec<?> other) {
            return this.magicByte == other.magicByte && this.packetClass.equals(other.packetClass);
        } else {
            return false;
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, WrappedUdpPipelineData msg, List<Object> out) throws Exception {
        try {
            ByteBuf data = msg.unwrapAndRecycle();

            CompositeByteBuf buffer = Unpooled.compositeBuffer(2)
                    .addComponent(true, this.getMagicByteBuf().retainedSlice())
                    .addComponent(true, data);

            out.add(new DatagramPacket(buffer, msg.remoteAddress()));
        } catch (Throwable throwable) {
            LOGGER.error("Error while encoding packet", throwable);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        short magic = this.readMagicByte(msg.content());
        if (magic < 0) {
            return; // Invalid magic byte
        }
        if ((this.magicByte & 0xFF) == magic) {
            out.add(new WrappedUdpPipelineData(ContextMap.newInstance(), msg.sender(), this, msg.content().retainedSlice()));
        }
    }

    public short readMagicByte(ByteBuf buf) {
        return buf.readUnsignedByte();
    }

    public void clearRestMagicByte(ByteBuf buf){
        // Default does nothing
    }

    public byte getMagicByte() {
        return this.magicByte;
    }

    public ByteBuf getMagicByteBuf() {
        return this.magicByteBuf;
    }

    public Class<T> getPacketClass() {
        return this.packetClass;
    }

    public boolean isPacketFromThisCodec(Object packet) {
        return this.packetClass.isInstance(packet.getClass());
    }
}
