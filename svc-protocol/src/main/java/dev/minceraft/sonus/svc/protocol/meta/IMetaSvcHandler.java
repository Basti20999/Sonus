package dev.minceraft.sonus.svc.protocol.meta;

public interface IMetaSvcHandler {

    default void handleAddCategoryPacket(AddCategorySvcPacket packet) {
    }

    default void handleAddGroupPacket(AddGroupSvcPacket packet) {
    }

    default void handleCreateGroupPacket(CreateGroupSvcPacket packet) {
    }

    default void handleJoinedGroupPacket(JoinedGroupSvcPacket packet) {
    }

    default void handleJoinGroupPacket(JoinGroupSvcPacket packet) {
    }

    default void handleLeaveGroupPacket(LeaveGroupSvcPacket packet) {
    }

    default void handlePlayerStatesPacket(PlayerStatesSvcPacket packet) {
    }

    default void handlePlayerStatePacket(PlayerStateSvcPacket packet) {
    }

    default void handleRemoveCategoryPacket(RemoveCategorySvcPacket packet) {
    }

    default void handleRemoveGroupPacket(RemoveGroupSvcPacket packet) {
    }

    default void handleRequestSecretPacket(RequestSecretSvcPacket packet) {
    }

    default void handleSecretPacket(SecretSvcPacket packet) {
    }

    default void handleUpdateStatePacket(UpdateStateSvcPacket packet) {
    }
}
