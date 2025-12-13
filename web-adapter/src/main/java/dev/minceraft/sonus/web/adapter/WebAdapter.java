package dev.minceraft.sonus.web.adapter;
// Created by booky10 in Sonus (02:19 10.08.2025)

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.adapter.AdapterInfo;
import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.common.audio.AudioCategory;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.Vec3d;
import dev.minceraft.sonus.common.data.WorldRotatedVec3d;
import dev.minceraft.sonus.web.adapter.config.WebConfig;
import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.minceraft.sonus.web.protocol.packets.clientbound.AudioPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.CategoryAddPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.CategoryRemovePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.KeepAlivePacket;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public class WebAdapter implements SonusAdapter {

    private final WebSessionManager sessions = new WebSessionManager(this);
    private final WebServer server = new WebServer(this);
    private @MonotonicNonNull ISonusService service;
    private @MonotonicNonNull AdapterInfo adapterInfo;

    @Override
    public void load(ISonusService service) {
        this.service = service;
        service.getConfigHolder().registerConfigTemplate("web", WebConfig.class, WebConfig::new);
    }

    private AdapterInfo buildAdapterInfo() {
        return new AdapterInfo(this.service.getConfig().getSubConfig(WebConfig.class).enabled);
    }

    @Override
    public void init(ISonusService service) {
        this.server.openSocket();

        this.service.getEventManager().registerListener(new WebSonusListener(this));
    }

    private void sendAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio, @Nullable Vec3d pos) {
        WebSocketConnection connection = this.sessions.getConnection(player.getUniqueId());
        if (connection == null) {
            return;
        }
        AudioPacket packet = new AudioPacket();
        packet.setChannelId(source.getSenderId());
        packet.setSenderId(source.getSenderId());
        packet.setCategoryId(source.getCategoryId());
        packet.setAudio(audio.asOpus(() -> connection.getProcessor(source.getSenderId())));
        packet.setPosition(pos);
        connection.sendPacket(packet);
    }

    @Override
    public void sendStaticAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio) {
        this.sendAudio(player, source, audio, null);
    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio, Vec3d pos) {
        this.sendAudio(player, source, audio, pos);
    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio) {
        WorldRotatedVec3d sourcePos = source.getPosition();
        // we can only support spatial audio with source position
        if (sourcePos != null) {
            this.sendAudio(player, source, audio, sourcePos);
        }
    }

    @Override
    public void sendAudioEnd(ISonusPlayer player, IAudioSource source, long sequence) {
        // No-op
    }

    @Override
    public void registerCategory(ISonusPlayer player, AudioCategory category) {
        WebSocketConnection connection = this.sessions.getConnection(player.getUniqueId());
        if (connection != null) {
            CategoryAddPacket packet = new CategoryAddPacket();
            packet.setCategory(category);
            connection.sendPacket(packet);
        }
    }

    @Override
    public void unregisterCategory(ISonusPlayer player, UUID categoryId) {
        WebSocketConnection connection = this.sessions.getConnection(player.getUniqueId());
        if (connection != null) {
            CategoryRemovePacket packet = new CategoryRemovePacket();
            packet.setCategoryId(categoryId);
            connection.sendPacket(packet);
        }
    }

    @Override
    public void sendKeepAlive(ISonusPlayer player, long currentTime) {
        WebSocketConnection connection = this.sessions.getConnection(player.getUniqueId());
        if (connection != null) {
            KeepAlivePacket packet = new KeepAlivePacket();
            packet.setId(currentTime);
            connection.sendPacket(packet);
        }
    }

    @Override
    public AdapterInfo getAdapterInfo() {
        if (this.adapterInfo == null) {
            this.adapterInfo = this.buildAdapterInfo();
        }
        return this.adapterInfo;
    }

    public ISonusService getService() {
        return this.service;
    }

    public WebSessionManager getSessions() {
        return this.sessions;
    }
}
