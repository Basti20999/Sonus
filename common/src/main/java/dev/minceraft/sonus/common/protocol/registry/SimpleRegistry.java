package dev.minceraft.sonus.common.protocol.registry;
// Created by booky10 in Sonus (01:46 17.07.2025)

import dev.minceraft.sonus.common.protocol.util.PacketDirection;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

@NullMarked
public class SimpleRegistry<D, T extends ProtocolMessage<?>> {

    private final Codec<D, T> codec;
    private final IdCodec<D> idCodec;

    private final List<Supplier<? extends T>> constructors;
    private final Map<Class<? extends T>, Integer> packetIds;
    private final BiConsumer<Integer, T> idMapper;

    private SimpleRegistry(Codec<D, T> codec, IdCodec<D> idCodec, List<Entry<? extends T>> packets, BiConsumer<Integer, T> idMapper) {
        this.codec = codec;
        this.idCodec = idCodec;
        this.constructors = packets.stream().<Supplier<? extends T>>map(Entry::ctor).toList();
        this.packetIds = new IdentityHashMap<>(packets.size());
        this.idMapper = idMapper;
        for (int i = 0; i < packets.size(); i++) {
            this.packetIds.put(packets.get(i).clazz(), i);
            this.idMapper.accept(i, packets.get(i).ctor().get());
        }
    }

    public T read(D data, PacketDirection direction) {
        int packetId = this.idCodec.decoder.applyAsInt(data);
        T packet = this.constructors.get(packetId).get();
        this.codec.decoder.accept(data, packet);

        if (packet instanceof BothBound bothBound) {
            bothBound.setDirection(direction);
        }
        return packet;
    }

    public void write(D data, T packet) {
        int packetId = this.packetIds.get(packet.getClass());
        this.idCodec.encoder.accept(data, packetId);
        this.codec.encoder.accept(data, packet);
    }

    public static final class Builder<D, T extends ProtocolMessage<?, ?>> {

        private final List<Entry<? extends T>> packets = new ArrayList<>();
        private @MonotonicNonNull Codec<D, T> codec;
        private @MonotonicNonNull IdCodec<D> idCodec;
        private BiConsumer<Integer, T> idMapper = (id, packet) -> {
        };

        public Builder() {
        }

        public SimpleRegistry<D, T> build() {
            if (this.codec == null) {
                throw new IllegalStateException("Codec must be set before building the registry.");
            }
            if (this.idCodec == null) {
                throw new IllegalStateException("IdCodec must be set before building the registry.");
            }
            return new SimpleRegistry<>(this.codec, this.idCodec, this.packets, this.idMapper);
        }

        public <Z extends T> Builder<D, T> register(Class<Z> clazz, Supplier<Z> ctor) {
            this.packets.add(new Entry<>(clazz, ctor));
            return this;
        }

        public Builder<D, T> codec(BiConsumer<D, T> decoder, BiConsumer<D, T> encoder) {
            this.codec = new Codec<>(decoder, encoder);
            return this;
        }

        public Builder<D, T> idCodec(Object2IntFunction<D> reader, ObjIntConsumer<D> writer) {
            this.idCodec = new IdCodec<>(reader, writer);
            return this;
        }

        public Builder<D, T> idMapper(BiConsumer<Integer, T> idMapper) {
            this.idMapper = idMapper;
            return this;
        }
    }

    private record Entry<T extends ProtocolMessage<?, ?>>(Class<T> clazz, Supplier<T> ctor) {}

    private record IdCodec<D>(ToIntFunction<D> decoder, ObjIntConsumer<D> encoder) {}

    private record Codec<D, T>(BiConsumer<D, T> decoder, BiConsumer<D, T> encoder) {}
}
