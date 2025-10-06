package dev.minceraft.sonus.plasmo.protocol.cipher;

import dev.minceraft.sonus.plasmo.protocol.tcp.data.EncryptionInfo;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

public record NoCipher() implements ICipher {

    public static final NoCipher INSTANCE = new NoCipher();

    @Override
    public ByteBuf encrypt(ByteBuf data) {
        return data.copy();
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return data.clone();
    }

    @Override
    public ByteBuf decrypt(ByteBuf data) {
        return data.copy();
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return data.clone();
    }

    @Override
    public @Nullable EncryptionInfo getEncryptionInfo() {
        return null;
    }
}
