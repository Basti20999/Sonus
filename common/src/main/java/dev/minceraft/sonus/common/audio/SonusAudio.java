package dev.minceraft.sonus.common.audio;
// Created by booky10 in Sonus (02:44 17.07.2025)

import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
public sealed interface SonusAudio {

    short[] pcm();

    short[] pcm(Supplier<AudioProcessor> processor);

    byte[] opus();

    byte[] opus(Supplier<AudioProcessor> processor);

    long sequenceNumber();

    SonusAudio withSequenceNumber(long sequenceNumber);

    boolean isZeroLength();

    record Pcm(short[] pcm, long sequenceNumber) implements SonusAudio {

        @Override
        public short[] pcm(Supplier<AudioProcessor> processor) {
            return this.pcm;
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
        public SonusAudio withSequenceNumber(long sequenceNumber) {
            return new SonusAudio.Opus(this.opus, sequenceNumber);
        }

        @Override
        public boolean isZeroLength() {
            return this.opus.length == 0;
        }
    }
}
