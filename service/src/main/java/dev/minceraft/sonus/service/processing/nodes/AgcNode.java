package dev.minceraft.sonus.service.processing.nodes;

import de.maxhenkel.speex4j.AutomaticGainControl;
import de.maxhenkel.speex4j.UnknownPlatformException;
import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.service.processing.AudioPipelineNode;

import java.io.IOException;

public class AgcNode implements AudioPipelineNode {

    private static final int TARGET = dbSample(-5.0D); // -5 dBFS

    private final AutomaticGainControl agc;

    public AgcNode() {
        try {
            this.agc = new AutomaticGainControl(SonusConstants.FRAME_SIZE, SonusConstants.SAMPLE_RATE);
            this.agc.setTarget(TARGET);
        } catch (IOException | UnknownPlatformException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public short[] process(short[] data) {
        this.agc.agc(data);
        return data;
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
}
