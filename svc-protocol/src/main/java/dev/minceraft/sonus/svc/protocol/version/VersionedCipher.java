package dev.minceraft.sonus.svc.protocol.version;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

public final class VersionedCipher {

    public static final int TAG_LEN_BITS_20 = 128;
    private static final String CIPHER_18 = "AES/CBC/PKCS5Padding";
    private static final String CIPHER_20 = "AES/GCM/NoPadding";

    public static String getCipher(int version) {
        return switch (version) {
            case 18 -> CIPHER_18;
            case 20 -> CIPHER_20;
            default -> throw new IllegalArgumentException("Unsupported version: " + version);
        };
    }

    public static Cipher createCipher(int version) throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(getCipher(version));
    }

    public static void initCipher(int version, Cipher cipher, int mode, SecretKeySpec keySpec, byte[] iv)
            throws InvalidAlgorithmParameterException, InvalidKeyException {
        cipher.init(mode, keySpec, getAlgorithmParameters(version, iv));
    }

    private static AlgorithmParameterSpec getAlgorithmParameters(int version, byte[] iv) {
        return switch (version) {
            case 18 -> new IvParameterSpec(iv);
            case 20 -> new GCMParameterSpec(TAG_LEN_BITS_20, iv);
            default -> throw new IllegalArgumentException("Unsupported version: " + version);
        };
    }

    public static int getIvSize(int version) {
        return switch (version) {
            case 18 -> 16;
            case 20 -> 12;
            default -> throw new IllegalArgumentException("Unsupported version: " + version);
        };
    }
}
