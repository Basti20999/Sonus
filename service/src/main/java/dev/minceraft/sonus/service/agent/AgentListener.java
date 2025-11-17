package dev.minceraft.sonus.service.agent;

import com.google.common.collect.Table;
import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.audio.IAudioProcessor;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.protocol.meta.IMetaHandler;
import dev.minceraft.sonus.protocol.meta.servicebound.AudioStreamMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.BackendTickMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.RegisterAudioCategoryMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.UpdateRoomDefinitionMessage;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.player.PlayerManager;
import dev.minceraft.sonus.service.player.SonusPlayer;
import dev.minceraft.sonus.service.server.SonusServer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@NullMarked
public class AgentListener implements IMetaHandler, AutoCloseable {

    private final SonusService service;
    private final SonusServer server;
    // TODO does using a single decoder for a whole server break stuff?
    private final IAudioProcessor processor;

    public AgentListener(SonusService service, SonusServer server) {
        this.service = service;
        this.server = server;
        this.processor = service.createAudioProcessor();
    }

    @Override
    public void handleBackendTick(BackendTickMessage message) {
        PlayerManager playerManager = this.service.getPlayerManager();

        Map<UUID, WorldVec3d> positions = message.getPositions();
        if (positions != null) {
            for (Map.Entry<UUID, WorldVec3d> entry : positions.entrySet()) {
                SonusPlayer player = playerManager.getPlayer(entry.getKey());
                if (player == null) {
                    continue;
                }
                player.setPosition(entry.getValue());
            }
        }
        Table<UUID, UUID, SonusPlayerState> perPlayerStates = message.getPerPlayerStates();
        if (perPlayerStates != null) {
            for (Map.Entry<UUID, Map<UUID, SonusPlayerState>> row : perPlayerStates.rowMap().entrySet()) {
                SonusPlayer player = playerManager.getPlayer(row.getKey());
                if (player != null) {
                    player.setStates(row.getValue());
                }
            }
        }
        Map<UUID, @Nullable String> teams = message.getTeams();
        if (teams != null) {
            for (Map.Entry<UUID, @Nullable String> entry : teams.entrySet()) {
                SonusPlayer player = playerManager.getPlayer(entry.getKey());
                if (player == null) {
                    continue;
                }
                player.setTeam(entry.getValue());
            }
        }
    }

    @Override
    public void handleUpdateRoomDefinition(UpdateRoomDefinitionMessage message) {
        this.service.getRoomManager().updateRoomDefinition(this.server.getUniqueId(), message.getDefinition());
    }

    @Override
    public void handleAudioStream(AudioStreamMessage message) {
        SonusPlayer player = this.service.getPlayerManager().getPlayer(message.getPlayerId());
        if (player == null) {
            return; // no player found
        }
        List<AudioStreamMessage.Frame> frames = message.getFrames();
        if (frames.isEmpty()) {
            return; // no frames sent
        }
        // ensure category is sent to the player if set
        UUID categoryId = message.getCategoryId();
        if (categoryId != null) {
            this.server.ensureCategory(player, categoryId);
        }

        // send all frames at once, the client will properly queue them TODO hopefully, test this
        IAudioSource source = new IAudioSource.Static(message.getChannelId());
        for (AudioStreamMessage.Frame frame : frames) {
            SonusAudio audio = frame.processAudio(this.processor);
            player.sendStaticAudio(source, audio);
        }
    }

    @Override
    public void handleRegisterAudioCategory(RegisterAudioCategoryMessage message) {
        this.server.registerCategory(message.getCategory());
    }

    @Override
    public void close() {
        try (this.processor) {
            // NO-OP
        }
    }
}
