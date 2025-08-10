package dev.minceraft.sonus.svc.adapter;
// Created by booky10 in Sonus (02:19 10.08.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.adapter.VoiceAdapter;
import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import dev.minceraft.sonus.common.audio.SonusAudio;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SvcAdapter implements VoiceAdapter {

    @Override
    public void sendAudio(IAudioSource source, SonusAudio audio) {

    }

    @Override
    public VoiceProtocolAdapter getProtocolAdapter() {
        return null;
    }
}
