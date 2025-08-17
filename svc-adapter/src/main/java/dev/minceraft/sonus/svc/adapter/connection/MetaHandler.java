package dev.minceraft.sonus.svc.adapter.connection;

import dev.minceraft.sonus.svc.protocol.meta.AddCategorySvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.AddGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.CreateGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.IMetaSvcHandler;
import dev.minceraft.sonus.svc.protocol.meta.JoinGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.JoinedGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.LeaveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStateSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStatesSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemoveCategorySvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemoveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RequestSecretSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.SecretSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.UpdateStateSvcPacket;

public class MetaHandler implements IMetaSvcHandler {

    private final SvcConnection connection;

    public MetaHandler(SvcConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleAddCategoryPacket(AddCategorySvcPacket packet) {
        IMetaSvcHandler.super.handleAddCategoryPacket(packet);
    }

    @Override
    public void handleAddGroupPacket(AddGroupSvcPacket packet) {
        IMetaSvcHandler.super.handleAddGroupPacket(packet);
    }

    @Override
    public void handleCreateGroupPacket(CreateGroupSvcPacket packet) {
        IMetaSvcHandler.super.handleCreateGroupPacket(packet);
    }

    @Override
    public void handleJoinedGroupPacket(JoinedGroupSvcPacket packet) {
        IMetaSvcHandler.super.handleJoinedGroupPacket(packet);
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
    public void handlePlayerStatesPacket(PlayerStatesSvcPacket packet) {
        IMetaSvcHandler.super.handlePlayerStatesPacket(packet);
    }

    @Override
    public void handlePlayerStatePacket(PlayerStateSvcPacket packet) {
        IMetaSvcHandler.super.handlePlayerStatePacket(packet);
    }

    @Override
    public void handleRemoveCategoryPacket(RemoveCategorySvcPacket packet) {
        IMetaSvcHandler.super.handleRemoveCategoryPacket(packet);
    }

    @Override
    public void handleRemoveGroupPacket(RemoveGroupSvcPacket packet) {
        IMetaSvcHandler.super.handleRemoveGroupPacket(packet);
    }

    @Override
    public void handleRequestSecretPacket(RequestSecretSvcPacket packet) {
        packet.getCompatibilityVersion()
    }

    @Override
    public void handleSecretPacket(SecretSvcPacket packet) {
        IMetaSvcHandler.super.handleSecretPacket(packet);
    }

    @Override
    public void handleUpdateStatePacket(UpdateStateSvcPacket packet) {
        IMetaSvcHandler.super.handleUpdateStatePacket(packet);
    }
}
