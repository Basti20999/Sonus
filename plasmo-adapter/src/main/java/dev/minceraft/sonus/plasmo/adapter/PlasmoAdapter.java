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
import dev.minceraft.sonus.plasmo.adapter.connection.PlasmoConnection;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SourceAudioEndPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.source.PlayerSourceInfo;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.source.StaticSourceInfo;
import dev.minceraft.sonus.plasmo.protocol.udp.bothbound.PingPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.udp.clientbound.SourceAudioPlasmoPacket;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class PlasmoAdapter implements SonusAdapter {

    private static final String ADDON_ID = "sonus-plasmo-adapter";

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
        PlasmoConnection connection = this.sessionManager.getConnectionByUniqueId(player.getUniqueId());
        if (connection == null) {
            return; // no plasmo session found
        }

        connection.registerSourceInfo(source.getSenderId(), () -> new PlayerSourceInfo(
                ADDON_ID,
                source.getSenderId(),
                connection.getDefaultSourceLine().getId(),
                null,
                (byte) 0,
                this.adapter.getCodecInfo(),
                true,
                true,
                0,
                this.sessionManager.buildPlayerInfo(player, player) // attach the sound to the player himself for static audio
        ));

        SourceAudioPlasmoPacket packet = new SourceAudioPlasmoPacket();
        packet.setDistance((short) this.getService().getConfig().getVoiceChatRange());
        packet.setAudioData(audio.opus(() -> connection.getProcessor(source.getSenderId())));
        packet.setSequenceNumber(audio.sequenceNumber());
        packet.setSourceId(source.getSenderId());
        packet.setSourceState((byte) 0);

        connection.sendPacket(packet);
    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio, Vec3d pos) {
        PlasmoConnection connection = this.sessionManager.getConnectionByUniqueId(player.getUniqueId());
        if (connection == null) {
            return; // no plasmo session found
        }
        connection.registerSourceInfo(source.getSenderId(), () -> new StaticSourceInfo(
                ADDON_ID,
                source.getSenderId(),
                connection.getDefaultSourceLine().getId(),
                null,
                (byte) 0,
                this.adapter.getCodecInfo(),
                false,
                true,
                0,
                pos,
                Vec3d.ZERO
        ));

        SourceAudioPlasmoPacket packet = new SourceAudioPlasmoPacket();
        packet.setDistance((short) this.getService().getConfig().getVoiceChatRange());
        packet.setAudioData(audio.opus(() -> connection.getProcessor(source.getSenderId())));
        packet.setSequenceNumber(audio.sequenceNumber());
        packet.setSourceId(source.getSenderId());
        packet.setSourceState((byte) 0);
        connection.sendPacket(packet);
    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio) {
        PlasmoConnection connection = this.sessionManager.getConnectionByUniqueId(player.getUniqueId());
        if (connection == null) {
            return; // no plasmo session found
        }

        connection.registerSourceInfo(source.getSenderId(), () -> {
            if (source instanceof ISonusPlayer speaker) {
                return new PlayerSourceInfo(
                        ADDON_ID,
                        source.getSenderId(),
                        connection.getDefaultSourceLine().getId(),
                        speaker.getName(player),
                        (byte) 0,
                        this.adapter.getCodecInfo(),
                        false,
                        true,
                        0,
                        this.sessionManager.buildPlayerInfo(player, speaker));
            }
            return new StaticSourceInfo(
                    ADDON_ID,
                    source.getSenderId(),
                    connection.getDefaultSourceLine().getId(),
                    null,
                    (byte) 0,
                    this.adapter.getCodecInfo(),
                    true,
                    true,
                    0,
                    Vec3d.ZERO,
                    Vec3d.ZERO
            );
        });


        SourceAudioPlasmoPacket packet = new SourceAudioPlasmoPacket();
        packet.setDistance((short) this.getService().getConfig().getVoiceChatRange());
        packet.setAudioData(audio.opus(() -> connection.getProcessor(source.getSenderId())));
        packet.setSequenceNumber(audio.sequenceNumber());
        packet.setSourceId(source.getSenderId());
        packet.setSourceState((byte) 0);

        connection.sendPacket(packet);
    }

    @Override
    public void sendAudioEnd(ISonusPlayer player, IAudioSource source, long sequence) {
        PlasmoConnection connection = this.sessionManager.getConnectionByUniqueId(player.getUniqueId());
        if (connection == null) {
            return; // no plasmo session found
        }

        SourceAudioEndPacket packet = new SourceAudioEndPacket();
        packet.setSequenceNumber(sequence);
        packet.setSourceId(source.getSenderId());

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
        PlasmoConnection connection = this.sessionManager.getConnectionByUniqueId(player.getUniqueId());
        if (connection == null) {
            return; // no plasmo session found
        }
        if (!connection.isConnected()) {
            return;
        }

        PingPlasmoPacket packet = new PingPlasmoPacket();
        packet.setTimestamp(currentTime);

        connection.sendPacket(packet);
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
}
