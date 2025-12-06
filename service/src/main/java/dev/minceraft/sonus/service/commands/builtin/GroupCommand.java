package dev.minceraft.sonus.service.commands.builtin;

import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.rooms.RoomAudioType;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.commands.Command;
import dev.minceraft.sonus.service.commands.LiteralCommandNode;
import dev.minceraft.sonus.service.player.SonusPlayer;
import dev.minceraft.sonus.service.rooms.TransientStaticRoom;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

import static dev.minceraft.sonus.service.commands.CommandNode.argument;
import static dev.minceraft.sonus.service.commands.CommandNode.literal;

public class GroupCommand extends Command {

    public GroupCommand() {
        super("groups");
    }

    @Override
    public LiteralCommandNode construct() {
        return literal(this.label)
                .requires(ctx -> ctx.sender().hasPermission("sonus.command.groups") &&
                        ctx.sender() instanceof SonusPlayer player && player.isConnected())
                .with(literal("list")
                        .requires(ctx -> ctx.sender().hasPermission("sonus.command.groups.list"))
                        .executes(ctx -> this.listGroups(ctx.service(), (SonusPlayer) ctx.sender())))
                .with(literal("create")
                        .requires(ctx -> ctx.sender().hasPermission("sonus.command.groups.create") &&
                                ((SonusPlayer) ctx.sender()).getPrimaryRoom() == null)
                        .executes()
                        .with(argument("password", ))
                ).with(literal("join")
                 .requires(ctx -> ctx.sender().hasPermission("sonus.command.groups.join") &&
                                ((SonusPlayer) ctx.sender()).getPrimaryRoom() == null))
                        .with(argument("password", )
    );
    }

    private boolean listGroups(SonusService service, SonusPlayer player) {
        Collection<IRoom> rooms = service.getRoomManager().getRooms(TransientStaticRoom.class);
    }

    private boolean createGroup(SonusService service, SonusPlayer player, String name, RoomAudioType type, @Nullable String password) {
        IRoom room = service.getRoomManager().createStaticRoom(name, password, type, false);
    }

    private boolean joinGroup(SonusService service, SonusPlayer player, String name, @Nullable String password) {
        IRoom room = service.getRoomManager().getRoom(roomId);
        boolean success = room != null && player.canAccessRoom(room, password);
        if (success) {

        }
    }
}
