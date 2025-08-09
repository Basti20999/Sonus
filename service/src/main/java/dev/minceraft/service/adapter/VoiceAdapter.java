package dev.minceraft.service.adapter;
// Created by booky10 in Sonus (02:23 17.07.2025)

import dev.minceraft.service.audio.SonusAudio;
import dev.minceraft.sonus.common.IAudioSource;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface VoiceAdapter {

    void sendAudio(IAudioSource source, SonusAudio audio);
}
