package dev.minceraft.sonus.svc.protocol.registries;

import dev.minceraft.sonus.common.protocol.registry.ContextedRegistry;
import dev.minceraft.sonus.common.protocol.tcp.holder.PmDataHolderBuf;
import dev.minceraft.sonus.common.version.Versioned;
import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.meta.SvcMetaPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.AddCategorySvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.AddGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.JoinedGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.PlayerStateSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.PlayerStatesSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.RemoveCategorySvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.RemoveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.RemovePlayerStatePacket;
import dev.minceraft.sonus.svc.protocol.meta.clientbound.SecretSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.servicebound.CreateGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.servicebound.JoinGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.servicebound.LeaveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.servicebound.RequestSecretSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.servicebound.UpdateStateSvcPacket;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public final class SvcMetaPacketRegistry {

    private static final Map<Key, Integer> PACKET_IDS = new HashMap<>();

    public static final ContextedRegistry<PmDataHolderBuf, SvcMetaPacket, SvcPacketContext> BUF_REGISTRY =
            ContextedRegistry.Builder.<PmDataHolderBuf, SvcMetaPacket, SvcPacketContext>createContext()
                    .codec((data, packet, ctx) -> packet.decode(data.getFirst(), ctx),
                            (data, packet, ctx) -> packet.encode(data.getFirst(), ctx))
                    .idCodec((holder, ctx) -> PACKET_IDS.get(holder.getSecond()),
                            (id, packet, ctx) -> { /**/ })
                    .idConsumer((id, sample) -> {
                        for (Versioned.VersionedKeyEntry<Key> entry : sample.getPluginMessageChannel().versionedKeys()) {
                            PACKET_IDS.put(entry.key(), id);
                        }
                    })
                    .register(AddCategorySvcPacket.class, AddCategorySvcPacket::new)
                    .register(AddGroupSvcPacket.class, AddGroupSvcPacket::new)
                    .register(CreateGroupSvcPacket.class, CreateGroupSvcPacket::new)
                    .register(JoinGroupSvcPacket.class, JoinGroupSvcPacket::new)
                    .register(JoinedGroupSvcPacket.class, JoinedGroupSvcPacket::new)
                    .register(LeaveGroupSvcPacket.class, LeaveGroupSvcPacket::new)
                    .register(PlayerStateSvcPacket.class, PlayerStateSvcPacket::new)
                    .register(PlayerStatesSvcPacket.class, PlayerStatesSvcPacket::new)
                    .register(RemovePlayerStatePacket.class, RemovePlayerStatePacket::new)
                    .register(RemoveCategorySvcPacket.class, RemoveCategorySvcPacket::new)
                    .register(RemoveGroupSvcPacket.class, RemoveGroupSvcPacket::new)
                    .register(RequestSecretSvcPacket.class, RequestSecretSvcPacket::new)
                    .register(SecretSvcPacket.class, SecretSvcPacket::new)
                    .register(UpdateStateSvcPacket.class, UpdateStateSvcPacket::new)
                    .build();
}
