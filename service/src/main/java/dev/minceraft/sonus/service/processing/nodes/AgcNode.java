package dev.minceraft.sonus.service.processing.nodes;

import de.maxhenkel.speex4j.AutomaticGainControl;
import de.maxhenkel.speex4j.UnknownPlatformException;
import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.service.processing.AudioPipelineNode;

import java.io.IOException;

public class AgcNode implements AudioPipelineNode {

    private final AutomaticGainControl agc;

    public AgcNode() {
        try {
            this.agc = new AutomaticGainControl(SonusConstants.FRAME_SIZE, SonusConstants.SAMPLE_RATE);
        } catch (IOException | UnknownPlatformException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public short[] process(short[] data) {
        this.agc.agc(data);
        return data;
    }
}
