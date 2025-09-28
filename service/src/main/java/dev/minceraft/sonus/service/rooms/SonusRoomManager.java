package dev.minceraft.sonus.service.rooms;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.rooms.RoomType;
import dev.minceraft.sonus.common.service.ISonusRoomManager;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.platform.IServer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SonusRoomManager implements ISonusRoomManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    public final SonusService service;
    private final Map<UUID, IRoom> rooms = new HashMap<>();

    public SonusRoomManager(SonusService service) {
        this.service = service;
    }

    public void init() {
        this.service.getScheduler().schedule(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    private void tick() {
        synchronized (this.rooms) {
            Set<IServer> servers = this.service.getPlatform().getServers();
            for (IServer server : servers) {
                if (this.rooms.containsKey(server.getUniqueId())) {
                    continue;
                }
                LOGGER.info("Creating room for server {} ({})", server.getName(), server.getUniqueId());
                AbstractRoom room = this.service.getPlatform().provideRoom(server);
                this.rooms.put(room.getId(), room);
            }
            Set<UUID> currentServers = new HashSet<>(servers.size());
            for (IServer server : servers) {
                currentServers.add(server.getUniqueId());
            }
            this.rooms.values().removeIf(server ->
                    server.getRoomType() == RoomType.SPECIAL_SERVER_OWNED &&
                            !currentServers.contains(server.getId()));
        }
    }

    @Override
    public IRoom getRoom(UUID uniqueId) {
        return this.rooms.get(uniqueId);
    }

    @Override
    public Collection<IRoom> getRooms() {
        return this.rooms.values();
    }

    @Override
    public boolean joinRoom(ISonusPlayer player, UUID roomId, @Nullable String password) {
        IRoom room;
        synchronized (this.rooms) {
            room = this.rooms.get(roomId);
        }
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
    public IRoom createStaticRoom(String name, @Nullable String password) {
        StaticRoom room = new StaticRoom(RoomType.PLAYER_OWNED);
        room.setName(name);
        room.setPassword(password);

        synchronized (this.rooms) {
            this.rooms.put(room.getId(), room);
        }
        return room;
    }

    @Override
    public void removeRoom(IRoom room) {
        synchronized (this.rooms) {
            this.rooms.remove(room.getId());
        }
    }
}
