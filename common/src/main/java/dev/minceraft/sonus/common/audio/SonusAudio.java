package dev.minceraft.sonus.common.audio;
// Created by booky10 in Sonus (02:44 17.07.2025)

import org.jspecify.annotations.NullMarked;

@NullMarked
public record SonusAudio(byte[] data, long sequenceNumber) {

    public SonusAudio(byte[] data) {
        this(data, -1L);
    }

    public SonusAudio withSequenceNumber(long sequenceNumber) {
        return new SonusAudio(this.data, sequenceNumber);
    }

    public SonusAudio withData(byte[] data) {
        return new SonusAudio(data, this.sequenceNumber);
    }

}
