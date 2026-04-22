package dev.minceraft.sonus.protocol.meta;
// Created by booky10 in Sonus (01:14 17.07.2025)

import dev.minceraft.sonus.protocol.meta.agentbound.PlayerConnectionStateMessage;
import dev.minceraft.sonus.protocol.meta.agentbound.PlayerVoicePingMessage;
import dev.minceraft.sonus.protocol.meta.agentbound.TriggerCommandUpdateMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.AudioStreamMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.BackendTickMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.RegisterAudioCategoryMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.UpdateRoomDefinitionMessage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IMetaHandler {

    default void handleBackendTick(BackendTickMessage message) {
    }

    default void handleUpdateRoomDefinition(UpdateRoomDefinitionMessage message) {
    }

    default void handleAudioStream(AudioStreamMessage message) {
    }

    default void handleRegisterAudioCategory(RegisterAudioCategoryMessage message) {
    }

    default void handlePlayerConnectionState(PlayerConnectionStateMessage message) {
    }

    default void handleTriggerCommandUpdate(TriggerCommandUpdateMessage message) {
    }

    default void handlePlayerVoicePing(PlayerVoicePingMessage message) {
    }
}
