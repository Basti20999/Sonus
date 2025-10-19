package dev.minceraft.sonus.svc.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.service.ISonusServiceEvents;
import dev.minceraft.sonus.svc.protocol.data.SvcPlayerState;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStateSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemovePlayerStatePacket;

import java.util.UUID;

public class SvcSonusListener implements ISonusServiceEvents {

    private final SvcAdapter adapter;

    public SvcSonusListener(SvcAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onPlayerSwitchBackend(UUID playerId) {
        this.adapter.getSessionManager().removeSession(playerId);
    }

    @Override
    public void onPlayerQuit(UUID playerId) {
        this.adapter.getSessionManager().removeSession(playerId);
    }

    @Override
    public void onPlayerStateUpdate(ISonusPlayer player) {
        if (!player.isConnected()) {
            RemovePlayerStatePacket packet = new RemovePlayerStatePacket();
            packet.setPlayerId(player.getUniqueId());

            this.adapter.getSessionManager().broadcastPacket(packet);
            return;
        }
        PlayerStateSvcPacket packet = new PlayerStateSvcPacket();
        SvcPlayerState state = this.adapter.buildPlayerState(player);
        packet.setState(state);

        this.adapter.getSessionManager().broadcastPacketSourced(player, packet);
    }

    @Override
    public void onGroupCreate(IRoom room) {
        this.adapter.getSessionManager().broadcastNewGroup(room);
    }

    @Override
    public void onGroupRemove(IRoom room) {
        this.adapter.getSessionManager().broadcastRemoveGroup(room);
    }
}
