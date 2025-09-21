package dev.minceraft.sonus.service.processing;

import de.maxhenkel.opus4j.OpusDecoder;
import de.maxhenkel.opus4j.OpusEncoder;
import de.maxhenkel.opus4j.UnknownPlatformException;
import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.service.SonusService;

import java.io.IOException;

public class AudioProcessor {

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

    public short[] decode(byte[] data) {
        return this.decoder.decode(data);
    }

    public byte[] encode(short[] pcm) {
        return this.encoder.encode(pcm);
    }

    public byte[] process(byte[] input, AudioPipelineNode node) {
        short[] decoded = this.decode(input);
        short[] processed = node.process(decoded);
        return this.encode(processed);
    }
}
