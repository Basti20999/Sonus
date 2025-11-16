package dev.minceraft.sonus.service.processing;

import de.maxhenkel.opus4j.OpusDecoder;
import de.maxhenkel.opus4j.OpusEncoder;
import de.maxhenkel.opus4j.UnknownPlatformException;
import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.common.audio.IAudioProcessor;
import dev.minceraft.sonus.service.SonusService;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;

@NullMarked
public final class AudioProcessor implements IAudioProcessor {

    private final OpusDecoder decoder;
    private final OpusEncoder encoder;

    public AudioProcessor(SonusService service) {
        try {
            this.decoder = new OpusDecoder(SonusConstants.SAMPLE_RATE, SonusConstants.CHANNELS);
            this.decoder.setFrameSize(SonusConstants.FRAME_SIZE);
            this.encoder = new OpusEncoder(SonusConstants.SAMPLE_RATE, SonusConstants.CHANNELS, OpusEncoder.Application.VOIP);
            this.encoder.setMaxPayloadSize(service.getConfig().getMtuSize());
            this.encoder.setMaxPacketLossPercentage(0.05f);
        } catch (IOException | UnknownPlatformException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public short[] decode(byte[] data) {
        return this.decoder.decode(data);
    }

    @Override
    public byte[] encode(short[] pcm) {
        return this.encoder.encode(pcm);
    }
}
