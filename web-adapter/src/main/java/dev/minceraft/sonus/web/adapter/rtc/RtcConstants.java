package dev.minceraft.sonus.web.adapter.rtc;
// Created by booky10 in Sonus (11:08 PM 02.03.2026)

import dev.minceraft.sonus.common.SonusConstants;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class RtcConstants {

    public static final int BITS_PER_SAMPLE = SonusConstants.BITS_PER_SAMPLE;
    public static final int SAMPLE_RATE = SonusConstants.SAMPLE_RATE;

    // send frames more often than normal sonus adapters because
    // we need to "queue" audio internally for proper mixing; by
    // reducing the frame interval we ensure the audio latency stays low
    public static final int FRAME_INTERVAL = 10;
    public static final int FRAMES_PER_SECOND = 1000 / FRAME_INTERVAL;
    public static final int FRAME_SIZE = SAMPLE_RATE / FRAMES_PER_SECOND;

    private RtcConstants() {
    }
}
