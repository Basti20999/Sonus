package dev.minceraft.sonus.agent.paper.audio;
// Created by booky10 in TjcSonus (23:29 17.11.2024)

import dev.minceraft.sonus.agent.paper.util.AudioConversionUtil;
import org.bukkit.util.NumberConversions;
import org.joml.Math;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

import static dev.minceraft.sonus.common.SonusConstants.FRAME_SIZE;
import static dev.minceraft.sonus.common.SonusConstants.SAMPLE_RATE;

@NullMarked
public class FadingAudioSupplier implements AudioSupplier {

    private static final short[] EMPTY_FRAME = new short[FRAME_SIZE];

    private final AudioSupplier delegate;
    private final Supplier<Boolean> active;

    private float sourceVolume = 1f;
    private float targetVolume = 1f;
    private int totalFadeTicks = 0;
    private int fadeTicks = 0;

    public FadingAudioSupplier(AudioSupplier delegate, Supplier<Boolean> active) {
        this.delegate = delegate;
        this.active = active;
    }

    public void setVolume(float volume) {
        this.sourceVolume = volume;
    }

    public void setFade(float targetVolume, float seconds) {
        this.targetVolume = targetVolume;
        this.totalFadeTicks = NumberConversions.ceil((SAMPLE_RATE * seconds) / FRAME_SIZE);
        this.fadeTicks = 0;
    }

    @Override
    public short @Nullable [] get() {
        if (!this.active.get()) {
            return null;
        }
        if (this.totalFadeTicks == 0 && this.sourceVolume == 1f) {
            return this.delegate.get();
        }
        if (this.totalFadeTicks == 0) {
            if (this.sourceVolume == 0f) {
                return EMPTY_FRAME;
            }
            short[] samples = this.delegate.get();
            if (samples == null) {
                return null; // pass on cancel signal
            }
            return AudioConversionUtil.adjustVolume(samples.clone(), this.sourceVolume);
        }
        short[] samples = this.delegate.get();
        if (samples == null) {
            return null;
        }
        float preFadeProgress = this.fadeTicks++ / (float) this.totalFadeTicks;
        float preVolume = Math.lerp(preFadeProgress, this.sourceVolume, this.targetVolume);
        float postFadeProgress = this.fadeTicks / (float) this.totalFadeTicks;
        float postVolume = Math.lerp(postFadeProgress, this.sourceVolume, this.targetVolume);
        if (this.fadeTicks >= this.totalFadeTicks) {
            this.totalFadeTicks = 0;
            this.sourceVolume = this.targetVolume;
        }
        return AudioConversionUtil.adjustVolumeLerp(
                samples.clone(), preVolume, postVolume);
    }

    public AudioSupplier getDelegate() {
        return this.delegate;
    }
}
