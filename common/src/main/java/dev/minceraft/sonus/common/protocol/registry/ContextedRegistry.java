package dev.minceraft.sonus.common.protocol.registry;
// Created by booky10 in Sonus (01:46 17.07.2025)

import dev.minceraft.sonus.common.protocol.util.ObjIntObjectConsumer;
import dev.minceraft.sonus.common.protocol.util.TriConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;

@NullMarked
public class ContextedRegistry<D, T extends ProtocolMessage<?>, C> {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final Codec<D, T, C> codec;
    private final IdCodec<D, C> idCodec;

    private final Int2ObjectMap<Supplier<? extends T>> constructors;
    private final Map<Class<? extends T>, Integer> packetIds;

    protected ContextedRegistry(Codec<D, T, C> codec, IdCodec<D, C> idCodec, List<Entry<? extends T>> packets, BiConsumer<Integer, T> idConsumer) {
        this.codec = codec;
        this.idCodec = idCodec;
        this.constructors = new Int2ObjectArrayMap<>(packets.size());
        this.packetIds = new IdentityHashMap<>(packets.size());

        for (Entry<? extends T> packet : packets) {
            this.constructors.put(packet.id(), packet.ctor());
            this.packetIds.put(packet.clazz(), packet.id());
            idConsumer.accept(packet.id(), packet.ctor().get());
        }
    }

    public @Nullable T read(D data, @Nullable C ctx) {
        int packetId = this.idCodec.decoder.applyAsInt(data, ctx);
        T packet = this.constructors.get(packetId).get();
        this.codec.decoder.accept(data, packet, ctx);
        return packet;
    }

    public void write(D data, T packet, @Nullable C ctx) {
        try {
            int packetId = this.packetIds.get(packet.getClass());
            this.idCodec.encoder.accept(data, packetId, ctx);
            this.codec.encoder.accept(data, packet, ctx);
        } catch (NullPointerException exception) {
            throw new IllegalArgumentException("The given packet is not registered: " + packet.getClass(), exception);
        }
    }

    public <A extends T> int getPacketId(Class<A> clazz) {
        return this.packetIds.get(clazz);
    }

    public static class Builder<D, T extends ProtocolMessage<?>, C, S extends Builder<D, T, C, S>> {

        protected final List<Entry<? extends T>> packets = new ArrayList<>();
        protected @MonotonicNonNull Codec<D, T, C> codec;
        protected @MonotonicNonNull IdCodec<D, C> idCodec;
        protected BiConsumer<Integer, T> idConsumer = (id, packet) -> {
        };
        protected int nextId = 0;

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
            return new ContextedRegistry<>(this.codec, this.idCodec, this.packets, this.idConsumer);
        }

        @SuppressWarnings("unchecked")
        protected S getThis() {
            return (S) this;
        }

        public <Z extends T> S idOffset(int firstId) {
            if (!this.packets.isEmpty()) {
                LOGGER.warn("Changed idOffset while already having packets registered");
            }
            this.nextId = firstId;

            return this.getThis();
        }

        public <Z extends T> S register(Class<Z> clazz, Supplier<Z> ctor) {
            this.packets.add(new Entry<>(this.nextId++, clazz, ctor));
            return this.getThis();
        }

        public <Z extends T> S register(int id, Class<Z> clazz, Supplier<Z> ctor) {
            if (this.nextId != 0) {
                LOGGER.warn("Registering packet with explicit id after packets have been registered with implicit ids is not recommended.");
            }
            this.packets.add(new Entry<>(id, clazz, ctor));
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

        public S idConsumer(BiConsumer<Integer, T> idMapper) {
            this.idConsumer = idMapper;
            return this.getThis();
        }
    }

    protected record Entry<T extends ProtocolMessage<?>>(int id, Class<T> clazz, Supplier<T> ctor) {
    }

    protected record IdCodec<D, C>(
            ToIntBiFunction<D, @Nullable C> decoder,
            ObjIntObjectConsumer<D, @Nullable C> encoder
    ) {
    }

    protected record Codec<D, T, C>(
            TriConsumer<D, T, @Nullable C> decoder,
            TriConsumer<D, T, @Nullable C> encoder
    ) {
    }
}
