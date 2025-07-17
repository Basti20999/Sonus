package dev.minceraft.sonus.protocol.registry;
// Created by booky10 in Sonus (01:46 17.07.2025)

import dev.minceraft.sonus.protocol.util.VarInt;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@NullMarked
public final class ProtocolRegistry<T extends ProtocolMessage<?>> {

    private final List<Supplier<? extends T>> constructors;
    private final Map<Class<? extends T>, Integer> packetIds;

    private ProtocolRegistry(List<Entry<? extends T>> packets) {
        this.constructors = packets.stream().<Supplier<? extends T>>map(Entry::ctor).toList();
        this.packetIds = new IdentityHashMap<>(packets.size());
        for (int i = 0; i < packets.size(); i++) {
            this.packetIds.put(packets.get(i).clazz(), i);
        }
    }

    public T readArray(byte[] array, int version) {
        ByteBuf buf = Unpooled.wrappedBuffer(array);
        try {
            return this.read(buf, version);
        } finally {
            buf.release();
        }
    }

    public byte[] writeAsArray(T packet, int version) {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
        try {
            this.write(buf, packet, version);
            byte[] array = new byte[buf.readableBytes()];
            buf.readBytes(array);
            return array;
        } finally {
            buf.release();
        }
    }

    public T read(ByteBuf buf) {
        return this.read(buf, VarInt.read(buf));
    }

    public T read(ByteBuf buf, int version) {
        int packetId = VarInt.read(buf);
        T packet = this.constructors.get(packetId).get();
        packet.decode(buf, version);
        return packet;
    }

    public void write(ByteBuf buf, T packet, int version) {
        int packetId = this.packetIds.get(packet.getClass());
        VarInt.write(buf, packetId);
        packet.encode(buf, version);
    }

    public static final class Builder<T extends ProtocolMessage<?>> {

        private final List<Entry<? extends T>> packets = new ArrayList<>();

        public Builder() {
        }

        public ProtocolRegistry<T> build() {
            return new ProtocolRegistry<>(this.packets);
        }

        public <Z extends T> Builder<T> register(Class<Z> clazz, Supplier<Z> ctor) {
            this.packets.add(new Entry<>(clazz, ctor));
            return this;
        }
    }

    private record Entry<T extends ProtocolMessage<?>>(Class<T> clazz, Supplier<T> ctor) {}
}
