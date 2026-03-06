package dev.minceraft.sonus.web.pion.ipc;
// Created by booky10 in Sonus (6:38 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.PionApi;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NullMarked
public final class IpcTypes {

    private IpcTypes() {
    }

    public static <T extends Enum<?>> T readEnum(ByteBuf buf, Class<T> clazz) {
        return readEnum(buf, clazz.getEnumConstants());
    }

    public static <T extends Enum<?>> T readEnum(ByteBuf buf, T[] values) {
        return values[buf.readUnsignedShort()];
    }

    public static void writeEnum(ByteBuf buf, Enum<?> val) {
        buf.writeShort(val.ordinal());
    }

    public static PionApi.IceServer readIceServer(ByteBuf buf) {
        return new PionApi.IceServer(
                readUtf8(buf),
                readNullable(buf, IpcTypes::readUtf8),
                readNullable(buf, IpcTypes::readUtf8)
        );
    }

    public static void writeIceServer(ByteBuf buf, PionApi.IceServer iceServer) {
        writeUtf8(buf, iceServer.url());
        writeNullable(buf, iceServer.user(), IpcTypes::writeUtf8);
        writeNullable(buf, iceServer.auth(), IpcTypes::writeUtf8);
    }

    public static <T> @Nullable T readNullable(ByteBuf buf, Decoder<T> decoder) {
        return buf.readBoolean() ? decoder.decode(buf) : null;
    }

    public static <T> void writeNullable(ByteBuf buf, @Nullable T val, Encoder<T> encoder) {
        if (val != null) {
            buf.writeBoolean(true);
            encoder.encode(buf, val);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static <T> List<T> readList(ByteBuf buf, Decoder<T> decoder) {
        int len = buf.readUnsignedShort();
        List<T> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            list.add(decoder.decode(buf));
        }
        return list;
    }

    public static <T> void writeCollection(ByteBuf buf, Collection<T> collection, Encoder<T> encoder) {
        buf.writeShort(collection.size());
        for (T element : collection) {
            encoder.encode(buf, element);
        }
    }

    public static void writeUtf8(ByteBuf buf, String string) {
        buf.writeShort(ByteBufUtil.utf8Bytes(string));
        buf.writeCharSequence(string, StandardCharsets.UTF_8);
    }

    public static String readUtf8(ByteBuf buf) {
        int length = buf.readUnsignedShort();
        String ret = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);
        buf.skipBytes(length);
        return ret;
    }

    @FunctionalInterface
    public interface Decoder<T> {

        T decode(ByteBuf buf);
    }

    @FunctionalInterface
    public interface Encoder<T> {

        void encode(ByteBuf buf, T value);
    }
}
