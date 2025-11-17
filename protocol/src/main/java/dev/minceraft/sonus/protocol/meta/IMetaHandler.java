package dev.minceraft.sonus.protocol.meta;
// Created by booky10 in Sonus (01:14 17.07.2025)

import dev.minceraft.sonus.protocol.meta.servicebound.AudioStreamMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.BackendTickMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.RegisterAudioCategoryMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.UpdateRoomDefinitionMessage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IMetaHandler {

    void handleBackendTick(BackendTickMessage message);

    void handleUpdateRoomDefinition(UpdateRoomDefinitionMessage message);

    void handleAudioStream(AudioStreamMessage message);

    void handleRegisterAudioCategory(RegisterAudioCategoryMessage message);
}
