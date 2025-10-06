package dev.minceraft.sonus.plasmo.adapter;

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.Vec3d;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlasmoAdapter implements SonusAdapter {

    private @MonotonicNonNull ISonusService service;
    private @MonotonicNonNull PlasmoProtocolAdapter adapter;
    private @MonotonicNonNull PlasmoSessionManager sessionManager;
    private @MonotonicNonNull PlasmoSonusListener serviceListener;

    @Override
    public void init(ISonusService service) {
        this.service = service;

        this.adapter = new PlasmoProtocolAdapter(this);
        this.sessionManager = new PlasmoSessionManager(this);
        this.serviceListener = new PlasmoSonusListener(this);

        this.service.getEventManager().registerListener(this.serviceListener);
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
    public PlasmoProtocolAdapter getProtocolAdapter() {
        return this.adapter;
    }

    public ISonusService getService() {
        return this.service;
    }

    public PlasmoSessionManager getSessionManager() {
        return this.sessionManager;
    }
}
