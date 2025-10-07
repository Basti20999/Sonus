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

@NullMarked
public final class TcpPacketRegistry {

    public static final SimpleRegistry<ByteBuf, TcpPlasmoPacket<?>> REGISTRY =
            SimpleRegistry.Builder.<ByteBuf, TcpPlasmoPacket<?>>createSimple()
                    .codec((buf, packet) -> packet.decode(buf), (buf, packet) -> packet.encode(buf))
                    .idCodec(ByteBuf::readByte, ByteBuf::writeByte)
                    .idOffset(1) // Plasmo index starts at 1
                    .register(ConnectionPacket.class, ConnectionPacket::new)
                    .register(PlayerInfoRequestPacket.class, PlayerInfoRequestPacket::new)
                    .register(ConfigPacket.class, ConfigPacket::new)
                    .register(ConfigPlayerInfoPacket.class, (ConfigPlayerInfoPacket::new))
                    .register(LanguageRequestPacket.class, LanguageRequestPacket::new)

                    .register(LanguagePacket.class, LanguagePacket::new)
                    .register(PlayerListPacket.class, PlayerListPacket::new)
                    .register(PlayerInfoUpdatePacket.class, PlayerInfoUpdatePacket::new)
                    .register(PlayerDisconnectPacket.class, PlayerDisconnectPacket::new)
                    .register(PlayerInfoPacket.class, PlayerInfoPacket::new)
                    .register(PlayerStatePacket.class, PlayerStatePacket::new)
                    .register(PlayerAudioEndPacket.class, PlayerAudioEndPacket::new)
                    .register(PlayerActivationDistancesPacket.class, PlayerActivationDistancesPacket::new)

                    .register(DistanceVisualizePacket.class, DistanceVisualizePacket::new)
                    .register(SourceInfoRequestPacket.class, SourceInfoRequestPacket::new)
                    .register(SourceInfoPacket.class, SourceInfoPacket::new)
                    .register(SelfSourceInfoPacket.class, SelfSourceInfoPacket::new)
                    .register(SourceAudioEndPacket.class, SourceAudioEndPacket::new)

                    .register(ActivationRegisterPacket.class, ActivationRegisterPacket::new)
                    .register(ActivationUnregisterPacket.class, ActivationUnregisterPacket::new)

                    .register(SourceLineRegisterPacket.class, SourceLineRegisterPacket::new)
                    .register(SourceLineUnregisterPacket.class, SourceLineUnregisterPacket::new)
                    .register(SourceLinePlayerAddPacket.class, SourceLinePlayerAddPacket::new)
                    .register(SourceLinePlayerRemovePacket.class, SourceLinePlayerRemovePacket::new)
                    .register(SourceLinePlayersListPacket.class, SourceLinePlayersListPacket::new)

                    .register(AnimatedActionBarPacket.class, AnimatedActionBarPacket::new)
                    .build();
}
