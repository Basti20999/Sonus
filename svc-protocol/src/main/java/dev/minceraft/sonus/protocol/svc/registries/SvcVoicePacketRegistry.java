package dev.minceraft.sonus.protocol.svc.registries;


import dev.minceraft.sonus.protocol.svc.voice.AuthenticateAckSvcPacket;
import dev.minceraft.sonus.protocol.svc.voice.AuthenticateSvcPacket;
import dev.minceraft.sonus.protocol.svc.voice.ConnectionCheckAckSvcPacket;
import dev.minceraft.sonus.protocol.svc.voice.ConnectionCheckSvcPacket;
import dev.minceraft.sonus.protocol.svc.voice.GroupSoundSvcPacket;
import dev.minceraft.sonus.protocol.svc.voice.KeepAliveSvcPacket;
import dev.minceraft.sonus.protocol.svc.voice.LocationSoundSvcPacket;
import dev.minceraft.sonus.protocol.svc.voice.MicSvcPacket;
import dev.minceraft.sonus.protocol.svc.voice.PingSvcPacket;
import dev.minceraft.sonus.protocol.svc.voice.PlayerSoundSvcPacket;
import dev.minceraft.sonus.protocol.svc.voice.SvcVoicePacket;
import dev.minceraft.sonus.common.protocol.registry.SimpleRegistry;
import dev.minceraft.sonus.common.protocol.util.VarInt;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SvcVoicePacketRegistry {

    public static final SimpleRegistry<ByteBuf, SvcVoicePacket<?>> REGISTRY =
            new SimpleRegistry.Builder<ByteBuf, SvcVoicePacket<?>>()
                    .codec((buf, packet) -> packet.decode(buf), (buf, packet) -> packet.encode(buf))
                    .idCodec(buf -> VarInt.read(buf), VarInt::write)
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
