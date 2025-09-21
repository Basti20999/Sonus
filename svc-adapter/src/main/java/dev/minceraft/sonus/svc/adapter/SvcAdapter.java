package dev.minceraft.sonus.svc.adapter;
// Created by booky10 in Sonus (02:19 10.08.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.config.YamlConfigHolder;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.Vec3d;
import dev.minceraft.sonus.svc.adapter.config.SvcConfig;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import dev.minceraft.sonus.svc.protocol.version.VersionManager;
import dev.minceraft.sonus.svc.protocol.voice.GroupSoundSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.LocationSoundSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.PlayerSoundSvcPacket;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SvcAdapter implements SonusAdapter {

    private @MonotonicNonNull SvcSessionManager sessionManager;
    private @MonotonicNonNull SvcSonusListener serviceListener;
    private @MonotonicNonNull SvcProtocolAdapter protocolAdapter;
    private @MonotonicNonNull ISonusService service;
    private @MonotonicNonNull YamlConfigHolder<SvcConfig> config;

    @Override
    public void init(ISonusService service) {
        this.service = service;
        this.sessionManager = new SvcSessionManager(this);
        this.serviceListener = new SvcSonusListener(this);
        this.protocolAdapter = new SvcProtocolAdapter(this);
        this.config = new YamlConfigHolder<>(SvcConfig.class, this.service.getDataDirectory().resolve("svc-config.yml"));

        this.service.getEventManager().registerListener(this.serviceListener);

        VersionManager.logSupportedVersions();
    }

    @Override
    public void sendStaticAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio) {
        SvcConnection connection = this.sessionManager.getConnection(player.getUniqueId());

        GroupSoundSvcPacket packet = new GroupSoundSvcPacket();
        packet.setChannelId(source.getSenderId());
        packet.setSender(source.getSenderId());
        packet.setData(audio.data());
        packet.setSequenceNumber(audio.sequenceNumber());

        connection.sendPacket(packet);
    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio, Vec3d pos) {
        SvcConnection connection = this.sessionManager.getConnection(player.getUniqueId());

        LocationSoundSvcPacket packet = new LocationSoundSvcPacket();
        packet.setChannelId(source.getSenderId());
        packet.setSender(source.getSenderId());
        packet.setData(audio.data());
        packet.setSequenceNumber(audio.sequenceNumber());
        packet.setLocation(pos);
        packet.setDistance((float) this.service.getConfig().getVoiceChatRange());

        connection.sendPacket(packet);
    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio) {
        SvcConnection connection = this.sessionManager.getConnection(player.getUniqueId());

        PlayerSoundSvcPacket packet = new PlayerSoundSvcPacket();
        packet.setChannelId(source.getSenderId());
        packet.setSender(source.getSenderId());
        packet.setData(audio.data());
        packet.setSequenceNumber(audio.sequenceNumber());

        packet.setDistance((float) this.service.getConfig().getVoiceChatRange());

        connection.sendPacket(packet);
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
