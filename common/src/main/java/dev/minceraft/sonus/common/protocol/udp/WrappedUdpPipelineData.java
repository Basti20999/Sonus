package dev.minceraft.sonus.common.protocol.udp;

import org.jspecify.annotations.NullMarked;

import java.net.InetSocketAddress;

@NullMarked
public record WrappedUdpPipelineData(
        UdpBasedContext<?> context,
        InetSocketAddress remoteAddress,
        AbstractMagicUdpCodec<?> codec,
        Object data
) {

    public WrappedUdpPipelineData(UdpBasedContext<?> context, InetSocketAddress remoteAddress, AbstractMagicUdpCodec<?> codec, Object data) {
        this.context = context;
        this.remoteAddress = remoteAddress;
        this.codec = codec;
        this.data = data;

        this.context.remoteAddress = remoteAddress;
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

