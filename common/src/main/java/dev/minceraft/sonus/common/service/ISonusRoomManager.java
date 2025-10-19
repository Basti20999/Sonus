package dev.minceraft.sonus.common.service;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.rooms.RoomAudioType;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface ISonusRoomManager {

    IRoom getRoom(UUID uniqueId);

    Collection<IRoom> getRooms();

    boolean joinRoom(ISonusPlayer player, UUID roomId, @Nullable String password);

    boolean joinPrimaryRoom(ISonusPlayer player, UUID roomId, @Nullable String password);

    IRoom createStaticPrimaryRoom(String name, @Nullable String password, RoomAudioType audioType);

    void removeRoom(IRoom room);

    void leavePrimaryRoom(ISonusPlayer player);
}
