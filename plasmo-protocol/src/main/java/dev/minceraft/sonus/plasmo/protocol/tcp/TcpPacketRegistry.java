package dev.minceraft.sonus.plasmo.protocol.tcp;


import dev.minceraft.sonus.common.protocol.registry.SimpleRegistry;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.ActivationRegisterPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.ActivationUnregisterPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.AnimatedActionBarPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.ConfigPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.ConfigPlayerInfoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.ConnectionPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.DistanceVisualizePacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.LanguagePacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.PlayerDisconnectPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.PlayerInfoRequestPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.PlayerInfoUpdatePacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.PlayerListPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SelfSourceInfoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SourceAudioEndPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SourceInfoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SourceLinePlayerAddPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SourceLinePlayerRemovePacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SourceLinePlayersListPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SourceLineRegisterPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.SourceLineUnregisterPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.LanguageRequestPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.PlayerActivationDistancesPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.PlayerAudioEndPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.PlayerInfoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.PlayerStatePacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.SourceInfoRequestPacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

import static dev.minceraft.sonus.common.protocol.registry.ContextedRegistry.MessageDirection.DECODE;
import static dev.minceraft.sonus.common.protocol.registry.ContextedRegistry.MessageDirection.ENCODE;
import static dev.minceraft.sonus.common.protocol.registry.ContextedRegistry.MessageDirection.NONE;

@NullMarked
public final class TcpPacketRegistry {

    public static final SimpleRegistry<ByteBuf, TcpPlasmoPacket<?>> REGISTRY =
            SimpleRegistry.Builder.<ByteBuf, TcpPlasmoPacket<?>>createSimple()
                    .codec((buf, packet) -> packet.decode(buf), (buf, packet) -> packet.encode(buf))
                    .idCodec(ByteBuf::readByte, ByteBuf::writeByte)
                    .idOffset(1) // Plasmo index starts at 1
                    .register(ConnectionPacket.class, ConnectionPacket::new, ENCODE)
                    .register(PlayerInfoRequestPacket.class, PlayerInfoRequestPacket::new, ENCODE)
                    .register(ConfigPacket.class, ConfigPacket::new, ENCODE)
                    .register(ConfigPlayerInfoPacket.class, ConfigPlayerInfoPacket::new, NONE)
                    .register(LanguageRequestPacket.class, LanguageRequestPacket::new, DECODE)

                    .register(LanguagePacket.class, LanguagePacket::new, ENCODE)
                    .register(PlayerListPacket.class, PlayerListPacket::new, ENCODE)
                    .register(PlayerInfoUpdatePacket.class, PlayerInfoUpdatePacket::new, ENCODE)
                    .register(PlayerDisconnectPacket.class, PlayerDisconnectPacket::new, ENCODE)
                    .register(PlayerInfoPacket.class, PlayerInfoPacket::new, DECODE)
                    .register(PlayerStatePacket.class, PlayerStatePacket::new, DECODE)
                    .register(PlayerAudioEndPacket.class, PlayerAudioEndPacket::new, DECODE)
                    .register(PlayerActivationDistancesPacket.class, PlayerActivationDistancesPacket::new, DECODE)

                    .register(DistanceVisualizePacket.class, DistanceVisualizePacket::new, ENCODE)
                    .register(SourceInfoRequestPacket.class, SourceInfoRequestPacket::new, DECODE)
                    .register(SourceInfoPacket.class, SourceInfoPacket::new, ENCODE)
                    .register(SelfSourceInfoPacket.class, SelfSourceInfoPacket::new, ENCODE)
                    .register(SourceAudioEndPacket.class, SourceAudioEndPacket::new, ENCODE)

                    .register(ActivationRegisterPacket.class, ActivationRegisterPacket::new, ENCODE)
                    .register(ActivationUnregisterPacket.class, ActivationUnregisterPacket::new, ENCODE)

                    .register(SourceLineRegisterPacket.class, SourceLineRegisterPacket::new, ENCODE)
                    .register(SourceLineUnregisterPacket.class, SourceLineUnregisterPacket::new, ENCODE)
                    .register(SourceLinePlayerAddPacket.class, SourceLinePlayerAddPacket::new, ENCODE)
                    .register(SourceLinePlayerRemovePacket.class, SourceLinePlayerRemovePacket::new, ENCODE)
                    .register(SourceLinePlayersListPacket.class, SourceLinePlayersListPacket::new, ENCODE)

                    .register(AnimatedActionBarPacket.class, AnimatedActionBarPacket::new, ENCODE)
                    .build();
}
