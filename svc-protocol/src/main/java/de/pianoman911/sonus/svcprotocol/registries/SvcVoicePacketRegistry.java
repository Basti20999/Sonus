package de.pianoman911.sonus.svcprotocol.registries;


import de.pianoman911.sonus.svcprotocol.voice.AuthenticateAckSvcPacket;
import de.pianoman911.sonus.svcprotocol.voice.AuthenticateSvcPacket;
import de.pianoman911.sonus.svcprotocol.voice.ConnectionCheckAckSvcPacket;
import de.pianoman911.sonus.svcprotocol.voice.ConnectionCheckSvcPacket;
import de.pianoman911.sonus.svcprotocol.voice.GroupSoundSvcPacket;
import de.pianoman911.sonus.svcprotocol.voice.KeepAliveSvcPacket;
import de.pianoman911.sonus.svcprotocol.voice.LocationSoundSvcPacket;
import de.pianoman911.sonus.svcprotocol.voice.MicSvcPacket;
import de.pianoman911.sonus.svcprotocol.voice.PingSvcPacket;
import de.pianoman911.sonus.svcprotocol.voice.PlayerSoundSvcPacket;
import de.pianoman911.sonus.svcprotocol.voice.SvcVoicePacket;
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
