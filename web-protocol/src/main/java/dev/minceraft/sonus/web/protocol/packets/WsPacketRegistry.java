package dev.minceraft.sonus.web.protocol.packets;

import dev.minceraft.sonus.common.protocol.registry.ContextedRegistry;
import dev.minceraft.sonus.common.protocol.util.VarInt;
import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.clientbound.AudioEndPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.AudioPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.CategoryAddPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.CategoryRemovePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.ConnectedPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.PositionUpdatePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomAddPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomJoinResponsePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomLeaveResponsePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.RoomRemovePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.StateRemovePacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.StateUpdatePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.KeepAlivePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.PingPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.InputEndPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.InputSoundPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomCreatePacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomJoinRequestPacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.RoomLeavePacket;
import dev.minceraft.sonus.web.protocol.packets.servicebound.StateInfoPacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

import static dev.minceraft.sonus.common.protocol.registry.ContextedRegistry.MessageDirection.ANY;
import static dev.minceraft.sonus.common.protocol.registry.ContextedRegistry.MessageDirection.DECODE;
import static dev.minceraft.sonus.common.protocol.registry.ContextedRegistry.MessageDirection.ENCODE;

@NullMarked
public final class WsPacketRegistry {

    private WsPacketRegistry() {
    }

    public static final ContextedRegistry<ByteBuf, WebSocketPacket, WsPacketContext> REGISTRY =
            ContextedRegistry.Builder.<ByteBuf, WebSocketPacket, WsPacketContext>createContext()
                    .codec((buf, packet, ctx) -> packet.decode(buf, ctx),
                            (buf, packet, ctx) -> packet.encode(buf, ctx))
                    .idCodec((buf, __) -> VarInt.read(buf),
                            (buf, id, __) -> VarInt.write(buf, id))
                    // clientbound
                    .register(AudioPacket.class, AudioPacket::new, ENCODE)
                    .register(CategoryAddPacket.class, CategoryAddPacket::new, ENCODE)
                    .register(CategoryRemovePacket.class, CategoryRemovePacket::new, ENCODE)
                    .register(ConnectedPacket.class, ConnectedPacket::new, ENCODE)
                    .register(PositionUpdatePacket.class, PositionUpdatePacket::new, ENCODE)
                    .register(RoomAddPacket.class, RoomAddPacket::new, ENCODE)
                    .register(RoomJoinResponsePacket.class, RoomJoinResponsePacket::new, ENCODE)
                    .register(RoomLeaveResponsePacket.class, RoomLeaveResponsePacket::new, ENCODE)
                    .register(RoomRemovePacket.class, RoomRemovePacket::new, ENCODE)
                    .register(StateRemovePacket.class, StateRemovePacket::new, ENCODE)
                    .register(StateUpdatePacket.class, StateUpdatePacket::new, ENCODE)
                    // commonbound
                    .register(KeepAlivePacket.class, KeepAlivePacket::new, ANY)
                    .register(PingPacket.class, PingPacket::new, ANY)
                    // servicebound
                    .register(InputSoundPacket.class, InputSoundPacket::new, DECODE)
                    .register(RoomCreatePacket.class, RoomCreatePacket::new, DECODE)
                    .register(RoomJoinRequestPacket.class, RoomJoinRequestPacket::new, DECODE)
                    .register(RoomLeavePacket.class, RoomLeavePacket::new, DECODE)
                    .register(StateInfoPacket.class, StateInfoPacket::new, DECODE)
                    // "newer" packets added at the bottom to increase backwards compat
                    .register(AudioEndPacket.class, AudioEndPacket::new, ENCODE)
                    .register(InputEndPacket.class, () -> InputEndPacket.INSTANCE, DECODE)
                    .build();
}
