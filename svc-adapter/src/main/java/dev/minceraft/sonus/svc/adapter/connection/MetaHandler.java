package dev.minceraft.sonus.svc.adapter.connection;

import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.svc.adapter.SvcProtocolAdapter;
import dev.minceraft.sonus.svc.protocol.meta.CreateGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.IMetaSvcHandler;
import dev.minceraft.sonus.svc.protocol.meta.JoinGroupSvcPacket;
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
        IMetaSvcHandler.super.handleCreateGroupPacket(packet);
    }

    @Override
    public void handleJoinGroupPacket(JoinGroupSvcPacket packet) {
        IMetaSvcHandler.super.handleJoinGroupPacket(packet);
    }

    @Override
    public void handleLeaveGroupPacket(LeaveGroupSvcPacket packet) {
        IMetaSvcHandler.super.handleLeaveGroupPacket(packet);
    }

    @Override
    public void handleRequestSecretPacket(RequestSecretSvcPacket packet) {
        // Version check is done in the handler
        ISonusService service = this.protocolAdapter.getAdapter().getService();
        InetSocketAddress remoteAddress = service.getUdpServer().getRemoteAddress();

        SecretSvcPacket secretSvcPacket = new SecretSvcPacket();
        secretSvcPacket.setSecret(this.connection.getSecret());
        secretSvcPacket.setServerPort(remoteAddress.getPort());
        secretSvcPacket.setPlayerId(this.connection.getPlayer().getUniqueId());
        secretSvcPacket.setCodec(this.protocolAdapter.getAdapter().getConfig().getCodec());
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
