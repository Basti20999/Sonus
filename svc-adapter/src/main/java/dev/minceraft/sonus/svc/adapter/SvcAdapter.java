package dev.minceraft.sonus.svc.adapter;
// Created by booky10 in Sonus (02:19 10.08.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.config.YamlConfigHolder;
import dev.minceraft.sonus.svc.adapter.config.SvcConfig;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SvcAdapter implements SonusAdapter {

    private final SvcSessionManager sessionManager = new SvcSessionManager(this);
    private final SvcSonusListener serviceListener = new SvcSonusListener(this);
    private @MonotonicNonNull SvcProtocolAdapter protocolAdapter;
    private @MonotonicNonNull ISonusService service;
    private @MonotonicNonNull YamlConfigHolder<SvcConfig> config;

    @Override
    public void init(ISonusService service) {
        this.service = service;
        this.protocolAdapter = new SvcProtocolAdapter(this);
        this.config = new YamlConfigHolder<>(SvcConfig.class, this.service.getDataDirectory().resolve("svc-config.yml"));

        this.service.getEventManager().registerListener(this.serviceListener);
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
        return this.config.getDelegate();
    }

    public SvcSessionManager getSessionManager() {
        return this.sessionManager;
    }
}
