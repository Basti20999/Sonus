package dev.minceraft.sonus.plasmo.protocol.udp;

import dev.minceraft.sonus.common.protocol.registry.SimpleRegistry;
import dev.minceraft.sonus.plasmo.protocol.udp.bothbound.CustomPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.udp.bothbound.PingPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.udp.clientbound.SelfAudioInfoPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.udp.clientbound.SourceAudioPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.udp.serverbound.PlayerAudioPlasmoPacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class UdpPlasmoRegistry {

    public static SimpleRegistry<ByteBuf, UdpPlasmoPacket<?>> REGISTRY =
            SimpleRegistry.Builder.<ByteBuf, UdpPlasmoPacket<?>>createSimple()
                    .codec((buf, packet) -> packet.decode(buf), (buf, packet) -> packet.encode(buf))
                    .idCodec(ByteBuf::readByte, ByteBuf::writeByte)
                    .register(0x01, PingPlasmoPacket.class, PingPlasmoPacket::new)
                    .register(0x02, PlayerAudioPlasmoPacket.class, PlayerAudioPlasmoPacket::new)
                    .register(0x03, SourceAudioPlasmoPacket.class, SourceAudioPlasmoPacket::new)
                    .register(0x04, SelfAudioInfoPlasmoPacket.class, SelfAudioInfoPlasmoPacket::new)
                    .register(0x100, CustomPlasmoPacket.class, CustomPlasmoPacket::new)
                    .build();

}
