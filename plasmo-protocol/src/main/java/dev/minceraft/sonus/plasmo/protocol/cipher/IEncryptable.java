package dev.minceraft.sonus.plasmo.protocol.cipher;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IEncryptable {

    void encrypt(ICipher cipher);

    void decrypt(ICipher cipher);
}
