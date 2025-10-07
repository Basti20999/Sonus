package dev.minceraft.sonus.svc.adapter.connection;

import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.service.ISonusRoomManager;
import dev.minceraft.sonus.svc.adapter.SvcProtocolAdapter;
import dev.minceraft.sonus.svc.protocol.meta.CreateGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.IMetaSvcHandler;
import dev.minceraft.sonus.svc.protocol.meta.JoinGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.JoinedGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.LeaveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RequestSecretSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.SecretSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.UpdateStateSvcPacket;

import java.net.InetSocketAddress;

public class MetaHandler implements IMetaSvcHandler {

    private final SvcProtocolAdapter protocolAdapter;
    private final SvcConnection connection;

    public MetaHandler(SvcProtocolAdapter protocolAdapter, SvcConnection connection) {
        this.protocolAdapter = protocolAdapter;
        this.connection = connection;
    }

    @Override
    public void handleCreateGroupPacket(CreateGroupSvcPacket packet) {
        IRoom room = this.protocolAdapter.getAdapter().getService().getRoomManager()
                .createStaticRoom(packet.getName(), packet.getPassword());
        room.setRoomType(packet.getType().toSonus());

        this.connection.getPlayer().joinRoom(room);
        this.connection.getPlayer().setCustomRoom(room);

        this.protocolAdapter.getAdapter().getSessionManager().broadcastNewGroup(room);

        this.protocolAdapter.getAdapter().getSessionManager().broadcastState(this.connection);

        JoinedGroupSvcPacket response = new JoinedGroupSvcPacket();
        response.setGroupId(room.getId());
        this.connection.sendPacket(response);
    }

    @Override
    public void handleJoinGroupPacket(JoinGroupSvcPacket packet) {
        ISonusRoomManager roomManager = this.protocolAdapter.getAdapter().getService().getRoomManager();
        boolean success = roomManager.joinRoom(this.connection.getPlayer(), packet.getGroupId(), packet.getPassword());

        JoinedGroupSvcPacket response = new JoinedGroupSvcPacket();
        response.setGroupId(success ? packet.getGroupId() : null);
        response.setWrongPassword(!success);
        this.connection.sendPacket(response);
        if (success) {
            this.connection.getPlayer().setCustomRoom(roomManager.getRoom(packet.getGroupId()));
            this.protocolAdapter.getAdapter().getSessionManager().broadcastState(this.connection);
        }
    }

    @Override
    public void handleLeaveGroupPacket(LeaveGroupSvcPacket packet) {
        IRoom customRoom = this.connection.getPlayer().getCustomRoom();
        if (customRoom == null) {
            return; // Not in a custom room
        }
        this.connection.getPlayer().leaveRoom(customRoom);
        this.connection.getPlayer().setCustomRoom(null);

        JoinedGroupSvcPacket response = new JoinedGroupSvcPacket();
        response.setGroupId(null);
        this.connection.sendPacket(response);

        this.protocolAdapter.getAdapter().getSessionManager().broadcastState(this.connection);

        if (customRoom.getMembers().isEmpty()) {
            this.protocolAdapter.getAdapter().getSessionManager().broadcastRemoveGroup(customRoom);
            this.protocolAdapter.getAdapter().getService().getRoomManager().removeRoom(customRoom);
        }
    }

    @Override
    public void handleRequestSecretPacket(RequestSecretSvcPacket packet) {
        // Version check is done in the handler
        this.connection.setVersion(packet.getCompatibilityVersion());

        ISonusService service = this.protocolAdapter.getAdapter().getService();
        InetSocketAddress remoteAddress = service.getUdpServer().getRemoteAddress();

        SecretSvcPacket secretSvcPacket = new SecretSvcPacket();
        secretSvcPacket.setSecret(this.connection.getSecret());
        secretSvcPacket.setServerPort(remoteAddress.getPort());
        secretSvcPacket.setPlayerId(this.connection.getPlayer().getUniqueId());
        secretSvcPacket.setCodec(this.protocolAdapter.getAdapter().getService().getConfig().getOpusCodec());
        secretSvcPacket.setMtuSize(service.getConfig().getMtuSize());
        secretSvcPacket.setKeepAlive(service.getConfig().getKeepAliveMs());
        secretSvcPacket.setGroupsEnabled(true); // Sonus requires groups to be enabled
        secretSvcPacket.setVoiceHost(remoteAddress.getHostString());
        secretSvcPacket.setAllowRecording(service.getConfig().allowRecordings());

        this.connection.sendPacket(secretSvcPacket);
    }

    @Override
    public void handleUpdateStatePacket(UpdateStateSvcPacket packet) {
        if (!this.connection.setDisabled(packet.isDisabled())) {
            return;
        }
        this.protocolAdapter.getAdapter().getSessionManager().broadcastState(this.connection);
    }
}
