package dev.minceraft.sonus.common;
// Created by booky10 in Sonus (01:50 17.07.2025)

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SonusConstants {

    public static final String PLUGIN_MESSAGE_CHANNEL = "sonus:agent";
    public static final int SAMPLE_RATE = 48000; // 48 kHz
    public static final int FRAME_SIZE = (SAMPLE_RATE / 1000) * 20;
    public static final int CHANNELS = 1; // Mono
    public static final int CURRENT_VERSION = 0;

    private SonusConstants() {
    }
}
