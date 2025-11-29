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
import dev.minceraft.sonus.plasmo.adapter.connection.PlasmoConnection;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SourceInfoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.source.StaticSourceInfo;
import dev.minceraft.sonus.plasmo.protocol.udp.clientbound.SourceAudioPlasmoPacket;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.util.UUID;

@NullMarked
public class PlasmoAdapter implements SonusAdapter {

    private static final String ADDON_ID = "sonus-plasmo-adapter";

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
        PlasmoConnection connection = this.sessionManager.getConnectionByUniqueId(player.getUniqueId());
        if (connection == null) {
            return; // no plasmo session found
        }

        UUID sourceId = UUID.randomUUID();
        StaticSourceInfo info = new StaticSourceInfo(
                ADDON_ID,
                sourceId,
                source.getSenderId(),
                null,
                (byte) 0,
                null,
                true,
                true,
                0,
                Vec3d.ZERO,
                Vec3d.ZERO
        );

        SourceInfoPacket infoPacket = new SourceInfoPacket();
        infoPacket.setSourceInfo(info);

        connection.sendPacket(infoPacket);

        SourceAudioPlasmoPacket packet = new SourceAudioPlasmoPacket();
        packet.setDistance((short) 0);
        packet.setAudioData(audio.opus(() -> connection.getProcessor(source.getSenderId())));
        packet.setSequenceNumber(audio.sequenceNumber());
        packet.setSourceId(sourceId);
        packet.setSourceState((byte) 0);

        connection.sendPacket(packet);
    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio, Vec3d pos) {

    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio) {
        PlasmoConnection connection = this.sessionManager.getConnectionByUniqueId(player.getUniqueId());
        if (connection == null) {
            return; // no plasmo session found
        }

        SourceAudioPlasmoPacket packet = new SourceAudioPlasmoPacket();
        packet.setDistance((short) 0);
        packet.setAudioData(audio.opus(() -> connection.getProcessor(source.getSenderId())));
        packet.setSequenceNumber(audio.sequenceNumber());
        packet.setSourceId(source.getSenderId());
        packet.setSourceState((byte) 0);

        connection.sendPacket(packet);
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
}
