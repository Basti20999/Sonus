package dev.minceraft.sonus.svc.protocol.registries;


import dev.minceraft.sonus.common.protocol.registry.SimpleRegistry;
import dev.minceraft.sonus.common.protocol.util.VarInt;
import dev.minceraft.sonus.svc.protocol.voice.AuthenticateAckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.AuthenticateSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.ConnectionCheckAckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.ConnectionCheckSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.GroupSoundSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.KeepAliveSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.LocationSoundSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.MicSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.PingSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.PlayerSoundSvcPacket;
import dev.minceraft.sonus.svc.protocol.voice.SvcVoicePacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SvcVoicePacketRegistry {

    public static final SimpleRegistry<ByteBuf, SvcVoicePacket<?>> REGISTRY =
            SimpleRegistry.Builder.<ByteBuf, SvcVoicePacket<?>>createSimple()
                    .codec((buf, packet) -> packet.decode(buf), (buf, packet) -> packet.encode(buf))
                    .idCodec(VarInt::read, VarInt::write)
                    .idOffset(1)  // Svc index starts at 1
                    .register(MicSvcPacket.class, MicSvcPacket::new)
                    .register(PlayerSoundSvcPacket.class, PlayerSoundSvcPacket::new)
                    .register(GroupSoundSvcPacket.class, GroupSoundSvcPacket::new)
                    .register(LocationSoundSvcPacket.class, LocationSoundSvcPacket::new)
                    .register(AuthenticateSvcPacket.class, AuthenticateSvcPacket::new)
                    .register(AuthenticateAckSvcPacket.class, AuthenticateAckSvcPacket::new)
                    .register(PingSvcPacket.class, PingSvcPacket::new)
                    .register(KeepAliveSvcPacket.class, KeepAliveSvcPacket::new)
                    .register(ConnectionCheckSvcPacket.class, ConnectionCheckSvcPacket::new)
                    .register(ConnectionCheckAckSvcPacket.class, ConnectionCheckAckSvcPacket::new)
                    .build();
}
