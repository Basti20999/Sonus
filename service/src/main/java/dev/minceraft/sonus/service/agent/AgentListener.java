package dev.minceraft.sonus.service.agent;

import com.google.common.collect.Multimap;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.protocol.meta.IMetaHandler;
import dev.minceraft.sonus.protocol.meta.servicebound.BackendTickMessage;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.player.PlayerManager;
import dev.minceraft.sonus.service.player.SonusPlayer;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@NullMarked
public class AgentListener implements IMetaHandler {

    private final SonusService service;

    public AgentListener(SonusService service) {
        this.service = service;
    }

    @Override
    public void handle(BackendTickMessage message) {
        PlayerManager playerManager = this.service.getPlayerManager();

        if (message.getPositions() != null) {
            for (Map.Entry<UUID, WorldVec3d> entry : message.getPositions().entrySet()) {
                SonusPlayer player = playerManager.getPlayer(entry.getKey());
                if (player == null) {
                    continue;
                }
                player.setPosition(entry.getValue());
            }
        }
        if (message.getPerPlayerStates() != null) {
            Multimap<UUID, SonusPlayerState> states = message.getPerPlayerStates();
            for (Map.Entry<UUID, Collection<SonusPlayerState>> entry : states.asMap().entrySet()) {
                SonusPlayer player = playerManager.getPlayer(entry.getKey());
                if (player == null) {
                    continue;
                }
                player.setStates(entry.getValue());
            }
        }
    }
}
