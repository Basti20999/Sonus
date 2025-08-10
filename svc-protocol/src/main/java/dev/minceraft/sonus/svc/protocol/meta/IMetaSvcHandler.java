package dev.minceraft.sonus.svc.protocol.meta;

import dev.minceraft.sonus.common.data.ISonusPlayer;

public interface IMetaSvcHandler {

    void handleAddCategoryPacket(ISonusPlayer player, AddCategorySvcPacket packet);

    void handleAddGroupPacket(ISonusPlayer player, AddGroupSvcPacket packet);

    void handleCreateGroupPacket(ISonusPlayer player, CreateGroupSvcPacket packet);

    void handleJoinedGroupPacket(ISonusPlayer player, JoinedGroupSvcPacket packet);

    void handleJoinGroupPacket(ISonusPlayer player, JoinGroupSvcPacket packet);

    void handleLeaveGroupPacket(ISonusPlayer player, LeaveGroupSvcPacket packet);

    void handlePlayerStatesPacket(ISonusPlayer player, PlayerStatesSvcPacket packet);

    void handlePlayerStatePacket(ISonusPlayer player, PlayerStateSvcPacket packet);

    void handleRemoveCategoryPacket(ISonusPlayer player, RemoveCategorySvcPacket packet);

    void handleRemoveGroupPacket(ISonusPlayer player, RemoveGroupSvcPacket packet);

    void handleRequestSecretPacket(ISonusPlayer player, RequestSecretSvcPacket packet);

    void handleSecretPacket(ISonusPlayer player, SecretSvcPacket packet);

    void handleUpdateStatePacket(ISonusPlayer player, UpdateStateSvcPacket packet);
}
