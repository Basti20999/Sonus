package dev.minceraft.sonus.svc.adapter;
// Created by booky10 in Sonus (02:19 10.08.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.adapter.VoiceAdapter;
import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.svc.adapter.config.SvcConfig;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SvcAdapter implements VoiceAdapter {

    private final ISonusService service;
    private final SvcConfig config;
    private final SvcSessionManager sessionManager = new SvcSessionManager(this);
    private final SvcProtocolAdapter protocolAdapter = new SvcProtocolAdapter(this);

    public SvcAdapter(ISonusService service) {
        this.service = service;
    }

    @Override
    public void sendAudio(IAudioSource source, SonusAudio audio) {

    }

    @Override
    public VoiceProtocolAdapter getProtocolAdapter() {
        return this.protocolAdapter;
    }

    public ISonusService getService() {
        return this.service;
    }

    public SvcConfig getConfig() {
        return this.config;
    }

    public SvcSessionManager getSessionManager() {
        return this.sessionManager;
    }
}
