package dev.minceraft.sonus.plasmo.adapter;

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.adapter.AdapterInfo;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.audio.AudioCategory;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.Vec3d;
import dev.minceraft.sonus.plasmo.adapter.config.PlasmoConfig;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.VoicePlayerInfo;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class PlasmoAdapter implements SonusAdapter {

    private @MonotonicNonNull ISonusService service;
    private @MonotonicNonNull PlasmoProtocolAdapter adapter;
    private @MonotonicNonNull PlasmoSessionManager sessionManager;
    private @MonotonicNonNull AdapterInfo adapterInfo;

    @Override
    public void load(ISonusService service) {
        this.service = service;
        service.getConfigHolder().registerConfigTemplate("plasmo", PlasmoConfig.class, PlasmoConfig::new);
    }

    private AdapterInfo buildAdapterInfo() {
        return new AdapterInfo(this.service.getConfig().getSubConfig(PlasmoConfig.class).enabled);
    }

    @Override
    public void init(ISonusService service) {
        this.adapter = new PlasmoProtocolAdapter(this);
        this.sessionManager = new PlasmoSessionManager(this);

        this.service.getEventManager().registerListener(new PlasmoSonusListener(this));
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
    public void sendKeepAlive(ISonusPlayer player, long currentTime) {
    }

    @Override
    public PlasmoProtocolAdapter getUdpAdapter() {
        return this.adapter;
    }

    @Override
    public AdapterInfo getAdapterInfo() {
        if (this.adapterInfo == null) {
            this.adapterInfo = this.buildAdapterInfo();
        }
        return this.adapterInfo;
    }

    public PlasmoConfig getConfig() {
        return this.service.getConfig().getSubConfig(PlasmoConfig.class);
    }

    public ISonusService getService() {
        return this.service;
    }

    public PlasmoSessionManager getSessionManager() {
        return this.sessionManager;
    }

    public VoicePlayerInfo buildPlayerInfo(ISonusPlayer player) {
        // TODO: respect viewer?
        return new VoicePlayerInfo(
                player.getUniqueId(),
                player.getName(),
                player.isMuted(),
                player.isConnected(),
                player.isDeafened()
        );
    }
}
