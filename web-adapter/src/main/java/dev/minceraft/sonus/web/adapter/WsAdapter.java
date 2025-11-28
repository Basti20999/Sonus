package dev.minceraft.sonus.web.adapter;
// Created by booky10 in Sonus (02:19 10.08.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import dev.minceraft.sonus.common.audio.AudioCategory;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.Vec3d;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class WsAdapter implements SonusAdapter {

    private @MonotonicNonNull ISonusService service;
    private final WebServer server = new WebServer(this);

    @Override
    public void init(ISonusService service) {
        this.service = service;

        this.server.openSocket();
    }

    @Override
    public void sendStaticAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio) {

    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio, Vec3d pos) {

    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio) {

    }

    @Override
    public void registerCategory(ISonusPlayer player, AudioCategory category) {

    }

    @Override
    public void unregisterCategory(ISonusPlayer player, UUID categoryId) {

    }

    @Override
    public VoiceProtocolAdapter getProtocolAdapter() {
        return null;
    }

    public ISonusService getService() {
        return this.service;
    }
}
