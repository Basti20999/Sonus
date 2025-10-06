package dev.minceraft.sonus.plasmo.protocol.cipher;

import dev.minceraft.sonus.plasmo.protocol.tcp.data.EncryptionInfo;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.Nullable;

public interface ICipher {

    ByteBuf encrypt(ByteBuf data);

    byte[] encrypt(byte[] data);

    ByteBuf decrypt(ByteBuf data);

    byte[] decrypt(byte[] data);

    @Nullable
    EncryptionInfo getEncryptionInfo();
}
