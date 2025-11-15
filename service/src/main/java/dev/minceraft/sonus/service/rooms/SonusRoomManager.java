package dev.minceraft.sonus.service.rooms;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.rooms.RoomAudioType;
import dev.minceraft.sonus.common.rooms.RoomType;
import dev.minceraft.sonus.common.rooms.options.RoomDefinition;
import dev.minceraft.sonus.common.service.ISonusRoomManager;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.platform.IServer;
import net.kyori.adventure.util.TriState;
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

import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_BYPASS_GROUP_PASSWORD;

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
                AbstractRoom room = new DefinedRoom(this.service, server.getUniqueId(),
                        RoomType.SPECIAL_SERVER_OWNED, new RoomDefinition());
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

    private IRoom joinRoom0(ISonusPlayer player, UUID roomId, @Nullable String password) {
        IRoom room;
        synchronized (this.rooms) {
            room = this.rooms.get(roomId);
        }
        if (room == null) {
            return null;
        }
        // check password
        if (!player.hasPermission(PERMISSION_BYPASS_GROUP_PASSWORD, TriState.NOT_SET)) {
            if (!Objects.equals(room.getPassword(), password)) {
                return null;
            }
        }
        player.joinRoom(room);
        return room;
    }

    @Override
    public boolean joinRoom(ISonusPlayer player, UUID roomId, @Nullable String password) {
        IRoom room = this.joinRoom0(player, roomId, password);
        return room != null;
    }

    @Override
    public boolean joinPrimaryRoom(ISonusPlayer player, UUID roomId, @Nullable String password) {
        IRoom room = this.joinRoom0(player, roomId, password);
        if (room != null) {
            player.setPrimaryRoom(room);
            player.updateState();
        }

        return room != null;
    }

    @Override
    public IRoom createStaticPrimaryRoom(String name, @Nullable String password, RoomAudioType audioType) {
        StaticRoom room = new StaticRoom(this.service, RoomType.PRIMARY);
        room.setName(name);
        room.setPassword(password);
        room.setRoomAudioType(audioType);

        synchronized (this.rooms) {
            this.rooms.put(room.getId(), room);
        }

        this.service.getEventManager().onGroupCreate(room);
        return room;
    }

    @Override
    public void leavePrimaryRoom(ISonusPlayer player) {
        IRoom primaryRoom = player.getPrimaryRoom();
        if (primaryRoom == null) {
            return; // Not in a primary room
        }
        player.leaveRoom(primaryRoom);
        player.setPrimaryRoom(null);
        this.service.getEventManager().onPlayerStateUpdate(player);

        if (primaryRoom.getMembers().isEmpty()) {
            this.removeRoom(primaryRoom);
        }
    }

    @Override
    public void removeRoom(IRoom room) {
        synchronized (this.rooms) {
            this.rooms.remove(room.getId());
        }
        this.service.getEventManager().onGroupRemove(room);
    }

    @Override
    public void updateRoomDefinition(UUID serverId, RoomDefinition definition) {
        IRoom room = this.getRoom(serverId);
        if (!(room instanceof DefinedRoom definedRoom)) {
            return;
        }
        definedRoom.updateDefinition(definition);
    }
}
