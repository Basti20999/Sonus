package dev.minceraft.sonus.common.audio;
// Created by booky10 in Sonus (02:44 17.07.2025)

import dev.minceraft.sonus.common.util.AudioConversionUtil;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
public sealed interface SonusAudio {

    short[] pcm();

    short[] pcm(Supplier<AudioProcessor> processor);

    default Pcm asPcm(Supplier<AudioProcessor> processor) {
        return new Pcm(this.pcm(processor), this.sequenceNumber());
    }

    byte[] opus();

    byte[] opus(Supplier<AudioProcessor> processor);

    default Opus asOpus(Supplier<AudioProcessor> processor) {
        return new Opus(this.opus(processor), this.sequenceNumber());
    }

    long sequenceNumber();

    SonusAudio withSequenceNumber(long sequenceNumber);

    boolean isZeroLength();

    record Pcm(short[] pcm, long sequenceNumber) implements SonusAudio {

        public Pcm(byte[] pcm, long sequenceNumber) {
            this(AudioConversionUtil.bytesToShorts(pcm), sequenceNumber);
        }

        @Override
        public short[] pcm(Supplier<AudioProcessor> processor) {
            return this.pcm;
        }

        @Override
        public Pcm asPcm(Supplier<AudioProcessor> processor) {
            return this;
        }

        @Override
        public byte[] opus() {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] opus(Supplier<AudioProcessor> processor) {
            return processor.get().encode(this.pcm);
        }

        @Override
        public SonusAudio withSequenceNumber(long sequenceNumber) {
            return new SonusAudio.Pcm(this.pcm, sequenceNumber);
        }

        @Override
        public boolean isZeroLength() {
            return this.pcm.length == 0;
        }
    }

    record Opus(byte[] opus, long sequenceNumber) implements SonusAudio {

        @Override
        public short[] pcm() {
            throw new UnsupportedOperationException();
        }

        @Override
        public short[] pcm(Supplier<AudioProcessor> processor) {
            return processor.get().decode(this.opus);
        }

        @Override
        public byte[] opus(Supplier<AudioProcessor> processor) {
            return this.opus;
        }

        @Override
        public Opus asOpus(Supplier<AudioProcessor> processor) {
            return this;
        }

        @Override
        public SonusAudio withSequenceNumber(long sequenceNumber) {
            return new SonusAudio.Opus(this.opus, sequenceNumber);
        }

        @Override
        public boolean isZeroLength() {
            return this.opus.length == 0;
        }
    }
}
