package dev.minceraft.sonus.common.service;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface ISonusRoomManager {

    IRoom getRoom(UUID uniqueId);

    boolean joinRoom(ISonusPlayer player, UUID roomId, @Nullable String password);

    IRoom createStaticRoom(String name, @Nullable String password);

    void removeRoom(IRoom room);
}
