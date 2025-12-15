package dev.minceraft.sonus.service.commands.builtin;

import dev.minceraft.sonus.common.rooms.IRoom;
import dev.minceraft.sonus.common.rooms.RoomAudioType;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.commands.Command;
import dev.minceraft.sonus.service.commands.LiteralCommandNode;
import dev.minceraft.sonus.service.commands.arguments.RoomTypeArgument;
import dev.minceraft.sonus.service.commands.arguments.StringArgument;
import dev.minceraft.sonus.service.player.SonusPlayer;
import dev.minceraft.sonus.service.rooms.TransientStaticRoom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static dev.minceraft.sonus.service.commands.CommandNode.argument;
import static dev.minceraft.sonus.service.commands.CommandNode.literal;

public class GroupCommand extends Command {

    public GroupCommand() {
        super("groups");
    }

    @Override
    public LiteralCommandNode construct() {
        return literal(this.label)
                .requires(ctx -> ctx.sender().hasPermission("sonus.command.groups")
                        && ctx.sender() instanceof SonusPlayer player && player.isConnected())
                .with(literal("list")
                        .requires(ctx -> ctx.sender().hasPermission("sonus.command.groups.list")
                                && ctx.sender() instanceof SonusPlayer player && player.getPrimaryRoom() == null)
                        .executes(ctx -> this.listGroups(ctx.service(), (SonusPlayer) ctx.sender())))
                .with(literal("create")
                        .requires(ctx -> ctx.sender().hasPermission("sonus.command.groups.create") &&
                                ((SonusPlayer) ctx.sender()).getPrimaryRoom() == null)
                        .with(argument("name", StringArgument.INSTANCE)
                                .requires(ctx -> ctx.sender().hasPermission("sonus.command.groups.create.passwordless"))
                                .with(argument("type", RoomTypeArgument.INSTANCE)
                                        .executes(ctx -> {
                                            SonusPlayer player = (SonusPlayer) ctx.sender();
                                            String name = ctx.get("name", StringArgument.INSTANCE);
                                            RoomAudioType type = ctx.get("type", RoomTypeArgument.INSTANCE);

                                            return this.createGroup(ctx.service(), player, name, type, null);
                                        }))
                                .with(argument("password", StringArgument.INSTANCE)
                                        .requires(ctx -> ctx.sender().hasPermission("sonus.command.groups.create.password"))
                                        .with(argument("type", RoomTypeArgument.INSTANCE)
                                                .executes(ctx -> {
                                                    SonusPlayer player = (SonusPlayer) ctx.sender();
                                                    String name = ctx.get("name", StringArgument.INSTANCE);
                                                    String password = ctx.get("password", StringArgument.INSTANCE);
                                                    RoomAudioType type = ctx.get("type", RoomTypeArgument.INSTANCE);

                                                    return this.createGroup(ctx.service(), player, name, type, password);
                                                }))))
                ).with(literal("join")
                        .requires(ctx -> ctx.sender().hasPermission("sonus.command.groups.join") &&
                                ((SonusPlayer) ctx.sender()).getPrimaryRoom() == null)
                        .with(argument("name", StringArgument.INSTANCE)
                                .with(argument("password", StringArgument.INSTANCE)
                                        .executes(ctx -> {
                                                    SonusPlayer player = (SonusPlayer) ctx.sender();
                                                    String name = ctx.get("name", StringArgument.INSTANCE);
                                                    String password = ctx.get("password", StringArgument.INSTANCE);

                                                    return this.joinGroup(ctx.service(), player, name, password);
                                                }
                                        )
                                ).executes(ctx -> {
                                    SonusPlayer player = (SonusPlayer) ctx.sender();
                                    String name = ctx.get("name", StringArgument.INSTANCE);

                                    return this.joinGroup(ctx.service(), player, name, null);
                                })))
                .with(literal("leave")
                .requires(ctx -> ctx.sender().hasPermission("sonus.command.groups.leave") &&
                        ((SonusPlayer) ctx.sender()).getPrimaryRoom() != null)
                .executes(ctx -> this.leaveGroup(ctx.service(), (SonusPlayer) ctx.sender())));
    }

    private boolean listGroups(SonusService service, SonusPlayer player) {
        Collection<IRoom> rooms = service.getRoomManager().getRooms(TransientStaticRoom.class);
        if (rooms.isEmpty()) {
            player.sendMessage(Component.translatable("sonus.command.groups.list.empty"));
        } else {
            Component message = Component.translatable("sonus.command.groups.list.header")
                    .arguments(Component.text(rooms.size()));
            for (IRoom room : rooms) {
                message = message.append(
                        Component.translatable("sonus.command.groups.list.entry")
                                .arguments(Component.text(room.getName()), Component.text(room.getMembers().size()))
                                .clickEvent(ClickEvent.callback(__ -> this.joinGroup(service, player, room.getName(), null)))
                );
            }

            player.sendMessage(message);
        }

        return true;
    }

    private boolean leaveGroup(SonusService service, SonusPlayer player) {
        player.setPrimaryRoom(null);
        player.sendMessage(Component.translatable("sonus.command.groups.leave.success"));
        return true;
    }

    private boolean createGroup(SonusService service, SonusPlayer player, String name, RoomAudioType type, @Nullable String password) {
        IRoom room = service.getRoomManager().createStaticRoom(name, password, type, false);
        return this.joinGroup(service, player, room.getId(), password);
    }

    private boolean joinGroup(SonusService service, SonusPlayer player, String name, @Nullable String password) {
        Set<IRoom> matched = new HashSet<>();
        for (IRoom room : service.getRoomManager().getRooms()) {
            if (room.getName().equalsIgnoreCase(name)) {
                matched.add(room);
            }
        }
        if (matched.isEmpty()) {
            player.sendMessage(Component.translatable("sonus.command.groups.join.not_found", name));
        } else if (matched.size() == 1) {
            IRoom room = matched.iterator().next();
            this.joinGroup(service, player, room.getId(), password);
        } else {
            Component message = Component.translatable("sonus.command.groups.join.multiple")
                    .arguments(Component.text(matched.size()));
            for (IRoom iRoom : matched) {
                message = message.append(
                        Component.translatable("sonus.command.groups.join.multiple.entry")
                                .arguments(Component.text(iRoom.getName()), Component.text(iRoom.getMembers().size()))
                                .clickEvent(ClickEvent.callback(__ -> this.joinGroup(service, player, name, password)))
                );
            }

            player.sendMessage(message);
        }

        return true;
    }

    private boolean joinGroup(SonusService service, SonusPlayer player, UUID roomId, @Nullable String password) {
        IRoom room = service.getRoomManager().getRoom(roomId);
        boolean success = room != null && player.canAccessRoom(room, password);
        if (success) {
            player.setPrimaryRoom(room);

            player.sendMessage(Component.translatable("sonus.command.groups.join.success", room.getName()));
        } else {
            player.sendMessage(Component.translatable("sonus.command.groups.join.failure"));
        }

        return true;
    }
}
