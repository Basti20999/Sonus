package dev.minceraft.sonus.agent.paper.audio;
// Created by booky10 in TjcSonus (22:53 17.11.2024)

import org.jspecify.annotations.NullMarked;

import static dev.minceraft.sonus.common.SonusConstants.FRAME_SIZE;

@NullMarked
public class RadioAudioSupplier implements AudioSupplier {

    private final short[] audioData;
    private final boolean alwaysTick;

    private final short[] frame = new short[FRAME_SIZE];
    private int position;

    public RadioAudioSupplier(short[] audioData, boolean alwaysTick) {
        this.audioData = audioData;
        this.alwaysTick = alwaysTick;
    }

    public void tick() {
        this.position = (this.position + FRAME_SIZE) % this.audioData.length;
    }

    @Override
    public short[] get() {
        if (this.alwaysTick) {
            this.tick();
        }

        int maxAudio = this.audioData.length - this.position;
        if (FRAME_SIZE > maxAudio) {
            System.arraycopy(this.audioData, this.position, this.frame, 0, maxAudio);
            System.arraycopy(this.audioData, 0, this.frame, maxAudio, FRAME_SIZE - maxAudio);
        } else {
            System.arraycopy(this.audioData, this.position, this.frame, 0, FRAME_SIZE);
        }
        return this.frame;
    }
}
