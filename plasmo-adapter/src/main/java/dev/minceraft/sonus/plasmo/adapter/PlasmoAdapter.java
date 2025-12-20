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
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SourceLineRegisterPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SourceLineUnregisterPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.VoiceSourceLine;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.source.DirectSourceInfo;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.source.PlayerSourceInfo;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.source.SourceInfo;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.source.StaticSourceInfo;
import dev.minceraft.sonus.plasmo.protocol.udp.bothbound.PingPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.udp.clientbound.SourceAudioPlasmoPacket;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.UUID;

import static dev.minceraft.sonus.plasmo.adapter.PlasmoConstants.ADDON_ID;

@NullMarked
public class PlasmoAdapter implements SonusAdapter {

    private final PlasmoTranslationHolder translationHolder = new PlasmoTranslationHolder();
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

        SourceInfo sourceInfo = connection.registerSourceInfo(source.getSenderId(), () -> new PlayerSourceInfo(
                ADDON_ID,
                source.getSenderId(),
                connection.getSourceLine(source.getCategoryId()).getId(),
                null,
                (byte) 0,
                this.adapter.getCodecInfo(),
                true,
                true,
                0,
                this.sessionManager.buildPlayerInfo(player, player) // attach the sound to the player himself for static audio
        ));

        this.sendAudio(connection, source, sourceInfo, audio);

        sourceInfo.resetState();
    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio, Vec3d pos) {
        PlasmoConnection connection = this.sessionManager.getConnectionByUniqueId(player.getUniqueId());
        if (connection == null) {
            return; // no plasmo session found
        }
        SourceInfo sourceInfo = connection.registerSourceInfo(source.getSenderId(), () -> {
            // TODO: Use direct SourceInfo for players, for better ux in the gui -> PlayerHeads
//            if (source instanceof ISonusPlayer speaker) {
//                return new DirectSourceInfo(
//                        ADDON_ID,
//                        source.getSenderId(),
//                        connection.getSourceLine(source.getCategoryId()).getId(),
//                        speaker.getName(player),
//                        (byte) 0,
//                        this.adapter.getCodecInfo(),
//                        true,
//                        false,
//                        0,
//                        speaker.getSimpleProfile(player),
//                        player POS,
//                        Vec3d.ZERO,
//                        false
//                );
//            }

            return new StaticSourceInfo(
                    ADDON_ID,
                    source.getSenderId(),
                    connection.getSourceLine(source.getCategoryId()).getId(),
                    null,
                    (byte) 0,
                    this.adapter.getCodecInfo(),
                    false,
                    false,
                    0,
                    pos,
                    Vec3d.ZERO
            );
        });

        if (sourceInfo instanceof StaticSourceInfo staticInfo) {
            if (staticInfo.getPosition().equals(pos)) {
                staticInfo.setPosition(pos);
                staticInfo.markDirty();
            }
        }

        this.sendAudio(connection, source, sourceInfo, audio);

        sourceInfo.resetState();
    }

    @Override
    public void sendSpatialAudio(ISonusPlayer player, IAudioSource source, SonusAudio audio) {
        PlasmoConnection connection = this.sessionManager.getConnectionByUniqueId(player.getUniqueId());
        if (connection == null) {
            return; // no plasmo session found
        }

        SourceInfo sourceInfo = connection.registerSourceInfo(source.getSenderId(), () -> {
            if (source instanceof ISonusPlayer speaker) {
                return new PlayerSourceInfo(
                        ADDON_ID,
                        source.getSenderId(),
                        connection.getSourceLine(source.getCategoryId()).getId(),
                        speaker.getName(player),
                        (byte) 0,
                        this.adapter.getCodecInfo(),
                        false,
                        true,
                        0,
                        this.sessionManager.buildPlayerInfo(player, speaker));
            }
            return new DirectSourceInfo(
                    ADDON_ID,
                    source.getSenderId(),
                    connection.getSourceLine(source.getCategoryId()).getId(),
                    null,
                    (byte) 0,
                    this.adapter.getCodecInfo(),
                    true,
                    false,
                    0,
                    null,
                    Vec3d.ZERO,
                    Vec3d.ZERO,
                    true
            );
        });

        this.sendAudio(connection, source, sourceInfo, audio);

        sourceInfo.resetState();
    }

    private void sendAudio(PlasmoConnection connection, IAudioSource source, SourceInfo sourceInfo, SonusAudio audio) {
        SourceAudioPlasmoPacket packet = new SourceAudioPlasmoPacket();
        packet.setDistance((short) this.getService().getConfig().getVoiceChatRange());
        packet.setAudioData(audio.opus(() -> connection.getProcessor(source.getSenderId())));
        packet.setSequenceNumber(audio.sequenceNumber());
        packet.setSourceId(source.getSenderId());
        packet.setSourceState(sourceInfo.getState());

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
        PlasmoConnection connection = this.sessionManager.getConnectionByUniqueId(player.getUniqueId());
        if (connection == null) {
            return; // no plasmo session found
        }
        VoiceSourceLine sourceLine = new VoiceSourceLine(
                category.getUniqueId().toString(),
                player.renderPlainComponent(category.getName()), // prerender the name component
                PlasmoConstants.DEFAULT_SOURCE_LINE_ICON,
                1.0,
                0,
                Set.of()
        );
        connection.registerVoiceSourceLine(category.getUniqueId(), sourceLine);

        SourceLineRegisterPacket packet = new SourceLineRegisterPacket();
        packet.setSourceLine(sourceLine);

        connection.sendPacket(packet);
    }

    @Override
    public void unregisterCategory(ISonusPlayer player, UUID categoryId) {
        PlasmoConnection connection = this.sessionManager.getConnectionByUniqueId(player.getUniqueId());
        if (connection == null) {
            return; // no plasmo session found
        }

        connection.unregisterVoiceSourceLine(categoryId);

        SourceLineUnregisterPacket packet = new SourceLineUnregisterPacket();
        packet.setSourceLineId(categoryId);
        connection.sendPacket(packet);
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

    public PlasmoTranslationHolder getTranslationHolder() {
        return this.translationHolder;
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
