package dev.minceraft.sonus.service.adapter;
// Created by booky10 in Sonus (02:23 17.07.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.service.audio.SonusAudio;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface VoiceAdapter {

    void sendAudio(IAudioSource source, SonusAudio audio);

    VoiceProtocolAdapter getProtocolAdapter();
}
