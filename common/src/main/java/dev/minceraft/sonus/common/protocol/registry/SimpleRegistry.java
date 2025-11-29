package dev.minceraft.sonus.common.protocol.registry;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;

@NullMarked
public class SimpleRegistry<D, T extends ProtocolMessage<?>> extends ContextedRegistry<D, T, Void> {

    protected SimpleRegistry(
            Codec<D, T, Void> codec, IdCodec<D, Void> idCodec,
            List<Entry<? extends T>> packets, BiConsumer<Integer, T> idMapper
    ) {
        super(codec, idCodec, packets, idMapper);
    }

    public @Nullable T decode(D data) {
        return this.decode(data, null); // context is null
    }

    public void encode(D data, T packet) {
        this.encode(data, packet, null); // context is null
    }

    public static final class Builder<D, T extends ProtocolMessage<?>, S extends Builder<D, T, S>> extends ContextedRegistry.Builder<D, T, Void, S> {

        private Builder() {
        }

        public static <D, T extends ProtocolMessage<?>> Builder<D, T, ?> createSimple() {
            return new Builder<>();
        }

        @Override
        public SimpleRegistry<D, T> build() {
            if (this.codec == null) {
                throw new IllegalStateException("Codec is not set");
            }
            if (this.idCodec == null) {
                throw new IllegalStateException("IdCodec is not set");
            }
            return new SimpleRegistry<>(this.codec, this.idCodec, List.copyOf(this.packets), this.idConsumer);
        }

        public S codec(BiConsumer<D, T> decoder, BiConsumer<D, T> encoder) {
            this.codec = new Codec<>(
                    (d, t, c) -> decoder.accept(d, t),
                    (d, t, c) -> encoder.accept(d, t));
            return this.getThis();
        }

        public S idCodec(ToIntFunction<D> reader, ObjIntConsumer<D> writer) {
            this.idCodec = new IdCodec<>(
                    (d, c) -> reader.applyAsInt(d),
                    (d, id, c) -> writer.accept(d, id));
            return this.getThis();
        }
    }
}
