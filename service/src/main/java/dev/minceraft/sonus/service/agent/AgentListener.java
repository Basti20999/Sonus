package dev.minceraft.sonus.service.agent;

import com.google.common.collect.Table;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.protocol.meta.IMetaHandler;
import dev.minceraft.sonus.protocol.meta.servicebound.BackendTickMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.UpdateRoomDefinitionMessage;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.platform.IServer;
import dev.minceraft.sonus.service.player.PlayerManager;
import dev.minceraft.sonus.service.player.SonusPlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@NullMarked
public class AgentListener implements IMetaHandler {

    private final SonusService service;
    private final IServer server;

    public AgentListener(SonusService service, IServer server) {
        this.service = service;
        this.server = server;
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
}
