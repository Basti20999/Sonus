package dev.minceraft.sonus.plasmo.protocol.cipher;

import dev.minceraft.sonus.plasmo.protocol.tcp.data.EncryptionInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public record CipherAes(EncryptionInfo encryptedInfo, SecretKeySpec key) implements ICipher {

    private static final String CIPHER = "AES/CBC/PKCS5Padding";
    private static final byte KEY_SIZE = 16;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final KeyFactory RSA_KEY_FACTORY;

    static {
        try {
            RSA_KEY_FACTORY = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static CipherAes createFromRsaHandshake(byte[] rsaPublicKey) {
        try {
            EncodedKeySpec keySpec = new X509EncodedKeySpec(rsaPublicKey);
            PublicKey publicKey = RSA_KEY_FACTORY.generatePublic(keySpec);

            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] key = new byte[KEY_SIZE];
            RANDOM.nextBytes(key);

            EncryptionInfo encryptionInfo = new EncryptionInfo(CIPHER, rsaCipher.doFinal(key));

            SecretKeySpec aes = new SecretKeySpec(key, "AES");

            return new CipherAes(encryptionInfo, aes);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public ByteBuf decrypt(ByteBuf buf) {
        byte[] data = ByteBufUtil.getBytes(buf);
        return Unpooled.wrappedBuffer(this.decrypt(data));
    }

    @Override
    public byte[] decrypt(byte[] data) {
        IvParameterSpec iv = this.ivFromEncrypted(data);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, this.key, iv);

            data = this.dataFromEncrypted(data);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public ByteBuf encrypt(ByteBuf buf) {
        byte[] data = ByteBufUtil.getBytes(buf);
        return Unpooled.wrappedBuffer(this.encrypt(data));
    }

    @Override
    public byte[] encrypt(byte[] data) {
        IvParameterSpec iv = this.generateIv();

        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, this.key, iv);

            byte[] encrypted = cipher.doFinal(data);
            return this.copyIvEncrypted(iv, encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public @Nullable EncryptionInfo getEncryptionInfo() {
        return this.encryptedInfo;
    }

    private IvParameterSpec generateIv() {
        byte[] iv = new byte[KEY_SIZE];
        RANDOM.nextBytes(iv);
        return new IvParameterSpec(iv);
    }


    private byte[] copyIvEncrypted(IvParameterSpec iv, byte[] encrypted) {
        byte[] encryptedIv = new byte[iv.getIV().length + encrypted.length];

        System.arraycopy(iv.getIV(), 0, encryptedIv, 0, iv.getIV().length);
        System.arraycopy(encrypted, 0, encryptedIv, iv.getIV().length, encrypted.length);

        return encryptedIv;
    }

    private IvParameterSpec ivFromEncrypted(byte[] encrypted) {
        byte[] iv = new byte[KEY_SIZE];
        System.arraycopy(encrypted, 0, iv, 0, iv.length);
        return new IvParameterSpec(iv);
    }

    private byte[] dataFromEncrypted(byte[] encrypted) {
        byte[] data = new byte[encrypted.length - KEY_SIZE];
        System.arraycopy(encrypted, KEY_SIZE, data, 0, data.length);
        return data;
    }
}
