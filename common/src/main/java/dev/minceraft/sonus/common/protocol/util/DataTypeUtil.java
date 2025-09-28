package dev.minceraft.sonus.common.protocol.util;
// Created by booky10 in Sonus (01:19 17.07.2025)

import com.google.common.collect.Multimap;
import dev.minceraft.sonus.common.util.GameProfile;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

@NullMarked
public class DataTypeUtil {

    private DataTypeUtil() {
    }

    public static UUID readUniqueId(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public static void writeUniqueId(ByteBuf buf, UUID uniqueId) {
        buf.writeLong(uniqueId.getMostSignificantBits());
        buf.writeLong(uniqueId.getLeastSignificantBits());
    }

    @SuppressWarnings("PatternValidation")
    public static Key readKey(ByteBuf buf) {
        return Key.key(Utf8String.read(buf));
    }

    public static void writeKey(ByteBuf buf, Key key) {
        Utf8String.write(buf, key.asMinimalString());
    }

    public static <K, V> Map<K, V> readMap(ByteBuf buf, BufReader<K> keyReader, BufReader<V> valueReader) {
        return readMap(buf, keyReader, valueReader, HashMap::new);
    }

    public static <M extends Map<K, V>, K, V> M readMap(
            ByteBuf buf,
            BufReader<K> keyReader,
            BufReader<V> valueReader,
            IntFunction<M> mapSupplier
    ) {
        int size = VarInt.read(buf);
        M map = mapSupplier.apply(size);
        for (int i = 0; i < size; i++) {
            K key = keyReader.read(buf);
            V value = valueReader.read(buf);
            map.put(key, value);
        }
        return map;
    }

    public static <M extends Multimap<K, V>, K, V> M readMultiMap(
            ByteBuf buf,
            BufReader<K> keyReader,
            BufReader<V> valueReader,
            Supplier<M> mapSupplier
    ) {
        Map<K, Collection<V>> read = readMap(buf, keyReader, b -> readCollection(b, valueReader, ArrayList::new), HashMap::new);
        M map = mapSupplier.get();
        read.forEach(map::putAll);
        return map;
    }

    public static <K, V> void writeMap(ByteBuf buf, Map<K, V> map, BufWriter<K> keyWriter, BufWriter<V> valueWriter) {
        VarInt.write(buf, map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            keyWriter.write(buf, entry.getKey());
            valueWriter.write(buf, entry.getValue());
        }
    }

    public static <K, V> void writeMultiMap(ByteBuf buf, Multimap<K, V> map, BufWriter<K> keyWriter, BufWriter<V> valueWriter) {
        writeMap(buf, map.asMap(), keyWriter, (b, col) -> writeCollection(b, col, valueWriter));
    }

    public static <C extends Collection<T>, T> C readCollection(ByteBuf buf, BufReader<T> reader, IntFunction<C> collectionSupplier) {
        int size = VarInt.read(buf);
        C collection = collectionSupplier.apply(size);
        for (int i = 0; i < size; i++) {
            T item = reader.read(buf);
            collection.add(item);
        }
        return collection;
    }

    public static <T> void writeCollection(ByteBuf buf, Collection<T> collection, BufWriter<T> writer) {
        VarInt.write(buf, collection.size());
        for (T item : collection) {
            writer.write(buf, item);
        }
    }

    public static <T> @Nullable T readIf(ByteBuf buf, Function<ByteBuf, T> reader) {
        if (buf.readBoolean()) {
            return reader.apply(buf);
        }
        return null;
    }

    public static <T> T readIfOrElse(ByteBuf buf, Function<ByteBuf, T> reader, Supplier<T> defaultValue) {
        return buf.readBoolean() ? reader.apply(buf) : defaultValue.get();
    }

    public static void writeIf(ByteBuf buf, boolean condition, Consumer<ByteBuf> writer) {
        buf.writeBoolean(condition);
        if (condition) {
            writer.accept(buf);
        }
    }

    public static <T> void writeNullable(ByteBuf buf, @Nullable T groupId, BiConsumer<ByteBuf, T> writer) {
        if (groupId != null) {
            buf.writeBoolean(true);
            writer.accept(buf, groupId);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static <T> void writeNullableIf(ByteBuf buf, @Nullable T value, Predicate<T> condition, BiConsumer<ByteBuf, T> writer) {
        if (value != null && condition.test(value)) {
            buf.writeBoolean(true);
            writer.accept(buf, value);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static void writeByteArray(ByteBuf buf, byte[] data) {
        if (data.length == 0) {
            VarInt.write(buf, 0);
        } else {
            VarInt.write(buf, data.length);
            buf.writeBytes(data);
        }
    }

    public static byte[] readByteArray(ByteBuf buf) {
        int length = VarInt.read(buf);
        if (length == 0) {
            return new byte[0];
        }
        byte[] data = new byte[length];
        buf.readBytes(data);
        return data;
    }

    public static byte[] readIntFramedByteArray(ByteBuf buf) {
        int read = buf.readInt();
        byte[] data = new byte[read];
        buf.readBytes(data);
        return data;
    }

    public static void writeIntFramedByteArray(ByteBuf buf, byte[] data) {
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }


    public static GameProfile readGameProfile(ByteBuf buf, Function<ByteBuf, String> stringReader) {
        UUID id = readUniqueId(buf);
        String name = stringReader.apply(buf);
        List<GameProfile.Property> properties = readCollection(buf, b -> readGameProfileProperty(b, stringReader), ArrayList::new);
        return new GameProfile(id, name, properties);
    }

    public static void writeGameProfile(ByteBuf buf, GameProfile profile, BiConsumer<ByteBuf, String> stringWriter) {
        writeUniqueId(buf, profile.uniqueId());
        stringWriter.accept(buf, profile.name());
        writeCollection(buf, profile.properties(), (b, property) -> writeGameProfileProperty(b, property, stringWriter));
    }

    public static GameProfile.Property readGameProfileProperty(ByteBuf buf, Function<ByteBuf, String> stringReader) {
        String name = stringReader.apply(buf);
        String value = stringReader.apply(buf);
        String signature = stringReader.apply(buf);
        return new GameProfile.Property(name, value, signature);
    }

    public static void writeGameProfileProperty(ByteBuf buf, GameProfile.Property property, BiConsumer<ByteBuf, String> stringWriter) {
        stringWriter.accept(buf, property.name());
        stringWriter.accept(buf, property.value());
        stringWriter.accept(buf, property.signature());
    }
}
