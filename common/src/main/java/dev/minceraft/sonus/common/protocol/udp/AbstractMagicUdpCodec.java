package dev.minceraft.sonus.common.protocol.udp;

import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import dev.minceraft.sonus.common.protocol.util.TypeUtil;
import io.leangen.geantyref.TypeToken;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public abstract class AbstractMagicUdpCodec<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final VoiceProtocolAdapter adapter;
    private final byte magicByte;
    private final ByteBuf magicByteBuf;
    private final Class<T> packetClass;

    public AbstractMagicUdpCodec(VoiceProtocolAdapter adapter, byte magicByte, Class<T> packetClass) {
        this.adapter = adapter;
        this.magicByte = magicByte;
        this.magicByteBuf = Unpooled.wrappedBuffer(new byte[]{magicByte});
        this.packetClass = packetClass;
    }

    public AbstractMagicUdpCodec(VoiceProtocolAdapter adapter, byte magicByte, TypeToken<T> packetTypeToken) {
        this.adapter = adapter;
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

    public short readMagicByte(ByteBuf buf) {
        return buf.readUnsignedByte();
    }

    public void clearRestMagicByte(ByteBuf buf) {
        // Default does nothing
    }

    public VoiceProtocolAdapter getAdapter() {
        return this.adapter;
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
