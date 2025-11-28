package dev.minceraft.sonus.web.protocol.packets;

import dev.minceraft.sonus.common.protocol.registry.ContextedRegistry;
import dev.minceraft.sonus.common.protocol.util.VarInt;
import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.clientbound.AudioPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.CategoryAddPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.CategoryRemovePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.ConnectedPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.PositionUpdatePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomAddPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomJoinResponsePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomRemovePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.StateUpdatePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.KeepAlivePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.PingPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.InputSoundPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomCreatePacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomJoinRequestPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomLeavePacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.StateInfoPacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WsPacketRegistry {

    private WsPacketRegistry() {
    }

    public static final ContextedRegistry<ByteBuf, WebsocketPacket, WsPacketContext> REGISTRY =
            ContextedRegistry.Builder.<ByteBuf, WebsocketPacket, WsPacketContext>createContext()
                    .codec((buf, packet, ctx) -> packet.decode(buf, ctx),
                            (buf, packet, ctx) -> packet.encode(buf, ctx))
                    .idCodec((buf, __) -> VarInt.read(buf),
                            (buf, id, __) -> VarInt.write(buf, id))
                    // clientbound
                    .register(AudioPacket.class, AudioPacket::new)
                    .register(CategoryAddPacket.class, CategoryAddPacket::new)
                    .register(CategoryRemovePacket.class, CategoryRemovePacket::new)
                    .register(ConnectedPacket.class, ConnectedPacket::new)
                    .register(PositionUpdatePacket.class, PositionUpdatePacket::new)
                    .register(RoomAddPacket.class, RoomAddPacket::new)
                    .register(RoomJoinResponsePacket.class, RoomJoinResponsePacket::new)
                    .register(RoomRemovePacket.class, RoomRemovePacket::new)
                    .register(StateUpdatePacket.class, StateUpdatePacket::new)
                    // commonbound
                    .register(KeepAlivePacket.class, KeepAlivePacket::new)
                    .register(PingPacket.class, PingPacket::new)
                    // servicebound
                    .register(InputSoundPacket.class, InputSoundPacket::new)
                    .register(RoomCreatePacket.class, RoomCreatePacket::new)
                    .register(RoomJoinRequestPacket.class, RoomJoinRequestPacket::new)
                    .register(RoomLeavePacket.class, RoomLeavePacket::new)
                    .register(StateInfoPacket.class, StateInfoPacket::new)
                    .build();
}
