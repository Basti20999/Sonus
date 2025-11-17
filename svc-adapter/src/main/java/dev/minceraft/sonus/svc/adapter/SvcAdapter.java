package dev.minceraft.sonus.svc.adapter;
// Created by booky10 in Sonus (02:19 10.08.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import dev.minceraft.sonus.common.audio.AudioCategory;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.Vec3d;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import dev.minceraft.sonus.svc.protocol.data.SonusVolumeCategory;
import dev.minceraft.sonus.svc.protocol.meta.AddCategorySvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemoveCategorySvcPacket;
import dev.minceraft.sonus.svc.protocol.version.VersionManager;
import dev.minceraft.sonus.svc.protocol.voice.GroupSoundSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.LocationSoundSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.PlayerSoundSvcPacket;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class SvcAdapter implements SonusAdapter {

    private @MonotonicNonNull SvcSessionManager sessions;
    private @MonotonicNonNull SvcProtocolAdapter protocolAdapter;
    private @MonotonicNonNull ISonusService service;

    @Override
    public void init(ISonusService service) {
        this.service = service;
        this.sessions = new SvcSessionManager(this);
        this.protocolAdapter = new SvcProtocolAdapter(this);

        this.service.getEventManager().registerListener(new SvcSonusListener(this));

        VersionManager.logSupportedVersions();
    }

    @Override
    public void sendStaticAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio) {
        SvcConnection connection = this.sessions.getConnection(player.getUniqueId());
        if (connection == null) {
            return; // no svc session found
        }

        GroupSoundSvcPacket packet = new GroupSoundSvcPacket();
        packet.setChannelId(source.getSenderId());
        packet.setSender(source.getSenderId());
        packet.setData(connection.getProcessor(source.getSenderId()).encode(audio.data()));
        packet.setSequenceNumber(audio.sequenceNumber());
        connection.sendPacket(packet);
    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio, Vec3d pos) {
        SvcConnection connection = this.sessions.getConnection(player.getUniqueId());
        if (connection == null) {
            return; // no svc session found
        }

        LocationSoundSvcPacket packet = new LocationSoundSvcPacket();
        packet.setChannelId(source.getSenderId());
        packet.setSender(source.getSenderId());
        packet.setData(connection.getProcessor(source.getSenderId()).encode(audio.data()));
        packet.setSequenceNumber(audio.sequenceNumber());
        packet.setLocation(pos);
        packet.setDistance((float) this.service.getConfig().getVoiceChatRange());
        connection.sendPacket(packet);
    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio) {
        SvcConnection connection = this.sessions.getConnection(player.getUniqueId());
        if (connection == null) {
            return; // no svc session found
        }

        PlayerSoundSvcPacket packet = new PlayerSoundSvcPacket();
        packet.setChannelId(source.getSenderId());
        packet.setSender(source.getSenderId());
        packet.setData(connection.getProcessor(source.getSenderId()).encode(audio.data()));
        packet.setSequenceNumber(audio.sequenceNumber());
        packet.setDistance((float) this.service.getConfig().getVoiceChatRange());
        connection.sendPacket(packet);
    }

    @Override
    public void registerCategory(ISonusPlayer player, AudioCategory category) {
        SvcConnection connection = this.sessions.getConnection(player.getUniqueId());
        if (connection == null) {
            return; // no svc session found
        }
        AddCategorySvcPacket packet = new AddCategorySvcPacket();
        packet.setCategory(new SonusVolumeCategory(category, player::renderPlainComponent));
        connection.sendPacket(packet);
    }

    @Override
    public void unregisterCategory(ISonusPlayer player, UUID categoryId) {
        SvcConnection connection = this.sessions.getConnection(player.getUniqueId());
        if (connection == null) {
            return; // no svc session found
        }
        RemoveCategorySvcPacket packet = new RemoveCategorySvcPacket();
        packet.setCategoryId(categoryId.toString());
        connection.sendPacket(packet);
    }

    @Override
    public VoiceProtocolAdapter getProtocolAdapter() {
        return this.protocolAdapter;
    }

    public ISonusService getService() {
        return this.service;
    }

    public SvcSessionManager getSessions() {
        return this.sessions;
    }
}
