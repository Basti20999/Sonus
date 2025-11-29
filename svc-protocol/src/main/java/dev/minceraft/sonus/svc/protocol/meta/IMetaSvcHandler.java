package dev.minceraft.sonus.svc.protocol.meta;

import dev.minceraft.sonus.svc.protocol.meta.clientbound.AddCategorySvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.AddGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.JoinedGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.PlayerStateSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.PlayerStatesSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.RemoveCategorySvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.RemoveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.RemovePlayerStatePacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.SecretSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.servicebound.CreateGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.servicebound.JoinGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.servicebound.LeaveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.servicebound.RequestSecretSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.servicebound.UpdateStateSvcPacket;

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

    default void handleRemovePlayerStatePacket(RemovePlayerStatePacket packet) {
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
