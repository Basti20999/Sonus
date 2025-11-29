package dev.minceraft.sonus.common.audio;

import de.maxhenkel.opus4j.OpusDecoder;
import de.maxhenkel.opus4j.OpusEncoder;
import de.maxhenkel.opus4j.UnknownPlatformException;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.util.function.IntSupplier;

import static dev.minceraft.sonus.common.SonusConstants.CHANNELS;
import static dev.minceraft.sonus.common.SonusConstants.FRAME_SIZE;
import static dev.minceraft.sonus.common.SonusConstants.SAMPLE_RATE;

/**
 * Initializatioon of decoder/encoder is not thread-safe!
 */
@NullMarked
public final class AudioProcessor implements AutoCloseable {

    private static final short[] ZERO_SHORT_ARRAY = new short[0];
    private static final byte[] ZERO_BYTE_ARRAY = new byte[0];

    private final IntSupplier mtu;
    private final Mode mode;

    private @MonotonicNonNull OpusDecoder decoder;
    private @MonotonicNonNull OpusEncoder encoder;

    public AudioProcessor(IntSupplier mtu, Mode mode) {
        this.mtu = mtu;
        this.mode = mode;
    }

    public short[] decode(byte[] data) {
        // 0 length data means state reset
        if (data.length == 0) {
            if (this.decoder != null) {
                this.decoder.resetState();
            }
            return ZERO_SHORT_ARRAY;
        }

        // lazy-load opus decoder
        if (this.decoder == null) {
            try {
                this.decoder = new OpusDecoder(SAMPLE_RATE, CHANNELS);
                this.decoder.setFrameSize(FRAME_SIZE);
            } catch (IOException | UnknownPlatformException exception) {
                throw new RuntimeException(exception);
            }
        }
        return this.decoder.decode(data);
    }

    public byte[] encode(short[] pcm) {
        // 0 length data means state reset
        if (pcm.length == 0) {
            if (this.encoder != null) {
                this.encoder.resetState();
            }
            return ZERO_BYTE_ARRAY;
        }

        // lazy-load opus encoder
        if (this.encoder == null) {
            try {
                this.encoder = new OpusEncoder(SAMPLE_RATE, CHANNELS, this.mode.asOpus());
                this.encoder.setMaxPayloadSize(this.mtu.getAsInt());
                this.encoder.setMaxPacketLossPercentage(0.05f);
            } catch (IOException | UnknownPlatformException exception) {
                throw new RuntimeException(exception);
            }
        }
        return this.encoder.encode(pcm);
    }

    @Override
    public void close() {
        try (OpusDecoder ignoredDecoder = this.decoder;
             OpusEncoder ignoredEncoder = this.encoder) {
            // NO-OP
        }
    }

    public enum Mode {

        VOICE,
        AUDIO,
        LOW_DELAY,
        ;

        private OpusEncoder.Application asOpus() {
            return switch (this) {
                case VOICE -> OpusEncoder.Application.VOIP;
                case AUDIO -> OpusEncoder.Application.AUDIO;
                case LOW_DELAY -> OpusEncoder.Application.LOW_DELAY;
            };
        }
    }
}
