package dev.minceraft.sonus.service.rooms;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.service.ISonusRoomManager;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SonusRoomManager implements ISonusRoomManager {

    private final Map<UUID, IRoom> rooms = new HashMap<>();

    @Override
    public IRoom getRoom(UUID uniqueId) {
        return this.rooms.get(uniqueId);
    }

    @Override
    public boolean joinRoom(ISonusPlayer player, UUID roomId, @Nullable String password) {
        IRoom room = this.rooms.get(roomId);
        if (room == null) {
            return false;
        }
        if (!Objects.equals(room.getPassword(), password)) {
            return false;
        }
        player.joinRoom(room);

        return true;
    }

    @Override
    public IRoom createGroupRoom(String name, @Nullable String password) {
        GroupRoom room = new GroupRoom();
        room.setName(name);
        room.setPassword(password);

        this.rooms.put(room.getId(), room);
        return room;
    }

    @Override
    public void removeRoom(IRoom room) {
        this.rooms.remove(room.getId());
    }
}
