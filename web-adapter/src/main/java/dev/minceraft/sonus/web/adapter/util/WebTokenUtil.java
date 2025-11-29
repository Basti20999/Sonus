package dev.minceraft.sonus.web.adapter.util;

import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public final class WebTokenUtil {

    public static final int TOKEN_LENGTH = 16;
    public static final char[] TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_.-".toCharArray();

    static {
        Arrays.sort(TOKEN_CHARS);
    }

    private WebTokenUtil() {
    }

    public static boolean isValidToken(String token) {
        if (token.length() != TOKEN_LENGTH) {
            return false; // immediately fail
        }
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int charIdx = Arrays.binarySearch(TOKEN_CHARS, token.charAt(i));
            if (charIdx < 0) {
                return false; // unknown character
            }
        }
        return true; // valid
    }

    public static String generateToken() {
        Random random = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            builder.append(TOKEN_CHARS[random.nextInt(TOKEN_CHARS.length)]);
        }
        return builder.toString();
    }
}
