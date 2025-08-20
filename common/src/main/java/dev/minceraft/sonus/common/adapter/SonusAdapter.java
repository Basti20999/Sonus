package dev.minceraft.sonus.common.adapter;
// Created by booky10 in Sonus (02:23 17.07.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.audio.SonusAudio;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface SonusAdapter {

    void init(ISonusService service);

    void sendAudio(IAudioSource source, SonusAudio audio);

    VoiceProtocolAdapter getProtocolAdapter();
}
