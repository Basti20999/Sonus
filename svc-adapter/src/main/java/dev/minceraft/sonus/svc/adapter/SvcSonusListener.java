package dev.minceraft.sonus.svc.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.service.ISonusServiceEvents;
import dev.minceraft.sonus.svc.protocol.data.SonusClientGroup;
import dev.minceraft.sonus.svc.protocol.meta.AddGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStateSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemoveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemovePlayerStatePacket;
import net.kyori.adventure.util.TriState;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_BYPASS_GROUP_PASSWORD;

@NullMarked
public class SvcSonusListener implements ISonusServiceEvents {

    private final SvcAdapter adapter;

    public SvcSonusListener(SvcAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onPlayerSwitchBackend(UUID playerId) {
        // remove backend-specific session
        this.adapter.getSessions().removeSession(playerId);
    }

    @Override
    public void onPlayerQuit(UUID playerId) {
        // remove backend-specific session
        this.adapter.getSessions().removeSession(playerId);
        // completely remove player
        RemovePlayerStatePacket packet = new RemovePlayerStatePacket();
        packet.setPlayerId(playerId);
        this.adapter.getSessions().broadcast(packet);
    }

    @Override
    public void onPlayerStateUpdate(ISonusPlayer player) {
        if (!player.isConnected()) {
            // if the player isn't connected and not in a primary room,
            // remove the state; if the player is in a primary room, still send state updates,
            // as the player would appear to be removed from the current primary room otherwise
            if (player.getPrimaryRoom() == null) {
                RemovePlayerStatePacket packet = new RemovePlayerStatePacket();
                packet.setPlayerId(player.getUniqueId());
                this.adapter.getSessions().broadcast(packet);
                return;
            }
        }
        // broadcast state update from the player
        this.adapter.getSessions().broadcastFrom(player, connection -> {
            PlayerStateSvcPacket packet = new PlayerStateSvcPacket();
            packet.setState(this.adapter.getSessions().buildPlayerState(connection.getPlayer(), player));
            return packet;
        });
    }

    @Override
    public void onGroupCreate(IRoom room) {
        this.adapter.getSessions().broadcast(connection -> {
            boolean bypassPassword = connection.getPlayer().hasPermission(PERMISSION_BYPASS_GROUP_PASSWORD, TriState.NOT_SET);
            AddGroupSvcPacket packet = new AddGroupSvcPacket();
            SonusClientGroup group = new SonusClientGroup(room, bypassPassword);
            packet.setGroup(group);
            return packet;
        });
    }

    @Override
    public void onGroupRemove(IRoom room) {
        RemoveGroupSvcPacket packet = new RemoveGroupSvcPacket();
        packet.setGroupId(room.getId());
        this.adapter.getSessions().broadcast(packet);
    }
}
