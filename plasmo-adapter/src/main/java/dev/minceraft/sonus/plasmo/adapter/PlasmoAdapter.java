package dev.minceraft.sonus.plasmo.adapter;

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.audio.AudioCategory;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.config.YamlConfigHolder;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.Vec3d;
import dev.minceraft.sonus.plasmo.adapter.config.PlasmoConfig;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.VoicePlayerInfo;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.UUID;

@NullMarked
public class PlasmoAdapter implements SonusAdapter {

    private @MonotonicNonNull ISonusService service;
    private @MonotonicNonNull PlasmoProtocolAdapter adapter;
    private @MonotonicNonNull PlasmoSessionManager sessionManager;
    private @MonotonicNonNull PlasmoSonusListener serviceListener;
    private @MonotonicNonNull YamlConfigHolder<PlasmoConfig> config;

    @Override
    public void init(ISonusService service) {
        this.service = service;

        this.adapter = new PlasmoProtocolAdapter(this);
        this.sessionManager = new PlasmoSessionManager(this);
        this.serviceListener = new PlasmoSonusListener(this);
        Path configPath = this.service.getDataDirectory().resolve("plasmo-config.yml");
        this.config = new YamlConfigHolder<>(PlasmoConfig.class, PlasmoConfig::new, configPath);

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

    public @MonotonicNonNull YamlConfigHolder<PlasmoConfig> getConfig() {
        return this.config;
    }

    public ISonusService getService() {
        return this.service;
    }

    public PlasmoSessionManager getSessionManager() {
        return this.sessionManager;
    }

    public VoicePlayerInfo buildPlayerInfo(ISonusPlayer player) {
        return new VoicePlayerInfo(
                player.getUniqueId(),
                player.getName(),
                player.isMuted(),
                player.isConnected(),
                player.isDeafened()
        );
    }
}
