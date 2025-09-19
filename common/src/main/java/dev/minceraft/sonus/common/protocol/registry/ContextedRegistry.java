package dev.minceraft.sonus.common.protocol.registry;
// Created by booky10 in Sonus (01:46 17.07.2025)

import dev.minceraft.sonus.common.protocol.util.ObjIntObjectConsumer;
import dev.minceraft.sonus.common.protocol.util.TriConsumer;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;

@NullMarked
public class ContextedRegistry<D, T extends ProtocolMessage<?>, C> {

    private final Codec<D, T, C> codec;
    private final IdCodec<D, C> idCodec;

    private final List<Supplier<? extends T>> constructors;
    private final Map<Class<? extends T>, Integer> packetIds;

    protected ContextedRegistry(Codec<D, T, C> codec, IdCodec<D, C> idCodec, List<Entry<? extends T>> packets, BiConsumer<Integer, T> idMapper) {
        this.codec = codec;
        this.idCodec = idCodec;
        this.constructors = packets.stream().<Supplier<? extends T>>map(Entry::ctor).toList();
        this.packetIds = new IdentityHashMap<>(packets.size());
        for (int i = 0; i < packets.size(); i++) {
            this.packetIds.put(packets.get(i).clazz(), i);
            idMapper.accept(i, packets.get(i).ctor().get());
        }
    }

    @Nullable
    public T read(D data, C ctx) {
        int packetId = this.idCodec.decoder.applyAsInt(data, ctx);
        T packet = this.constructors.get(packetId).get();
        this.codec.decoder.accept(data, packet, ctx);
        return packet;
    }

    public void write(D data, T packet, C ctx) {
        try {
            int packetId = this.packetIds.get(packet.getClass());
            this.idCodec.encoder.accept(data, packetId, ctx);
            this.codec.encoder.accept(data, packet, ctx);
        } catch (NullPointerException exception) {
            throw new IllegalArgumentException("The given packet is not registered: " + packet.getClass(), exception);
        }
    }

    public static class Builder<D, T extends ProtocolMessage<?>, C, S extends Builder<D, T, C, S>> {

        protected final List<Entry<? extends T>> packets = new ArrayList<>();
        protected @MonotonicNonNull Codec<D, T, C> codec;
        protected @MonotonicNonNull IdCodec<D, C> idCodec;
        protected BiConsumer<Integer, T> idMapper = (id, packet) -> {
        };

        protected Builder() {
        }

        public static <D, T extends ProtocolMessage<?>, C> Builder<D, T, C, ?> createContext() {
            return new Builder<>();
        }

        public ContextedRegistry<D, T, C> build() {
            if (this.codec == null) {
                throw new IllegalStateException("Codec must be set before building the registry.");
            }
            if (this.idCodec == null) {
                throw new IllegalStateException("IdCodec must be set before building the registry.");
            }
            return new ContextedRegistry<>(this.codec, this.idCodec, this.packets, this.idMapper);
        }

        @SuppressWarnings("unchecked")
        protected S getThis() {
            return (S) this;
        }

        public <Z extends T> S register(Class<Z> clazz, Supplier<Z> ctor) {
            this.packets.add(new Entry<>(clazz, ctor));
            return this.getThis();
        }

        public S codec(TriConsumer<D, T, C> decoder, TriConsumer<D, T, C> encoder) {
            this.codec = new Codec<>(decoder, encoder);
            return this.getThis();
        }

        public S idCodec(ToIntBiFunction<D, C> reader, ObjIntObjectConsumer<D, C> writer) {
            this.idCodec = new IdCodec<>(reader, writer);
            return this.getThis();
        }

        public S idMapper(BiConsumer<Integer, T> idMapper) {
            this.idMapper = idMapper;
            return this.getThis();
        }
    }

    protected record Entry<T extends ProtocolMessage<?>>(Class<T> clazz, Supplier<T> ctor) {}

    protected record IdCodec<D, C>(ToIntBiFunction<D, C> decoder, ObjIntObjectConsumer<D, C> encoder) {}

    protected record Codec<D, T, C>(TriConsumer<D, T, C> decoder, TriConsumer<D, T, C> encoder) {}
}
