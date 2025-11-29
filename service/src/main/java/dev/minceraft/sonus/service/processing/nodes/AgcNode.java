package dev.minceraft.sonus.service.processing.nodes;

import de.maxhenkel.speex4j.AutomaticGainControl;
import de.maxhenkel.speex4j.UnknownPlatformException;
import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.service.processing.AudioPipelineNode;

import java.io.IOException;

public final class AgcNode implements AudioPipelineNode, AutoCloseable {

    private static final int TARGET = dbSample(-5d); // -5 dBFS

    private final AutomaticGainControl agc;

    public AgcNode() {
        try {
            this.agc = new AutomaticGainControl(SonusConstants.FRAME_SIZE, SonusConstants.SAMPLE_RATE);
            this.agc.setTarget(TARGET);
        } catch (IOException | UnknownPlatformException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static int dbSample(double dbfs) {
        double value = 32768D * Math.pow(10D, dbfs / 20D);
        long rounded = Math.round(value);
        if (rounded < 0) {
            return 0;
        }
        if (rounded > 32768) {
            return 32768;
        }
        return (int) rounded;
    }

    @Override
    public void process(SonusAudio audio) {
        if (audio instanceof SonusAudio.Pcm pcm) {
            this.agc.agc(pcm.pcm());
        } else {
            throw new UnsupportedOperationException("Only able to process AGC for PCM audio");
        }
    }

    @Override
    public void close() {
        try (this.agc) {
            // NO-OP
        }
    }
}
