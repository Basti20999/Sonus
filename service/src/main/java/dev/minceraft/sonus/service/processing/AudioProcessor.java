package dev.minceraft.sonus.service.processing;

import de.maxhenkel.opus4j.OpusDecoder;
import de.maxhenkel.opus4j.OpusEncoder;
import de.maxhenkel.opus4j.UnknownPlatformException;
import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.common.audio.IAudioProcessor;
import dev.minceraft.sonus.service.SonusService;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;

@NullMarked
public final class AudioProcessor implements IAudioProcessor {

    private static final short[] ZERO_SHORT_ARRAY = new short[0];
    private static final byte[] ZERO_BYTE_ARRAY = new byte[0];

    private final SonusService service;
    private @MonotonicNonNull OpusDecoder decoder;
    private @MonotonicNonNull OpusEncoder encoder;

    public AudioProcessor(SonusService service) {
        this.service = service;
    }

    @Override
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
                this.decoder = new OpusDecoder(SonusConstants.SAMPLE_RATE, SonusConstants.CHANNELS);
                this.decoder.setFrameSize(SonusConstants.FRAME_SIZE);
            } catch (IOException | UnknownPlatformException exception) {
                throw new RuntimeException(exception);
            }
        }
        return this.decoder.decode(data);
    }

    @Override
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
                this.encoder = new OpusEncoder(SonusConstants.SAMPLE_RATE, SonusConstants.CHANNELS, OpusEncoder.Application.VOIP);
                this.encoder.setMaxPayloadSize(service.getConfig().getMtuSize());
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
}
