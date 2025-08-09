package dev.minecraft.sonus.common.protocol.udp;

import dev.minecraft.sonus.common.protocol.codec.IUdpPacket;
import dev.minecraft.sonus.common.protocol.util.ContextMap;
import org.jspecify.annotations.NullMarked;

import java.net.InetSocketAddress;

@NullMarked
public record WrappedUdpPipelineData(
        ContextMap context,
        InetSocketAddress remoteAddress,
        AbstractMagicUdpCodec<?> codec,
        Object data
) {

    public WrappedUdpPipelineData(ContextMap context, InetSocketAddress remoteAddress, AbstractMagicUdpCodec<?> codec, Object data) {
        this.context = context;
        this.remoteAddress = remoteAddress;
        this.codec = codec;
        this.data = data;

        this.context.put("remote", remoteAddress);
    }

    public static WrappedUdpPipelineData fromUdpPacket(IUdpPacket packet) {
        return new WrappedUdpPipelineData(ContextMap.newInstance(), packet.getRemoteAddress(), packet.getCodec(), packet);
    }

    @SuppressWarnings("unchecked")
    public <T> T unwrap() {
        return (T) this.data;
    }

    public <T> T unwrapAndRecycle() {
        T packet = this.unwrap();
        this.context.recycle();
        return packet;
    }

    public WrappedUdpPipelineData withPacket(Object packet) {
        return new WrappedUdpPipelineData(this.context, this.remoteAddress, this.codec, packet);
    }

    @Override
    public String toString() {
        return "WrappedUdpPipelineData{" +
                "context=" + context +
                ", remoteAddress=" + remoteAddress +
                ", codec=" + codec +
                ", data=" + data +
                '}';
    }
}

