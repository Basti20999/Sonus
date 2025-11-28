package dev.minceraft.sonus.svc.protocol.registries;

import dev.minceraft.sonus.common.protocol.registry.ContextedRegistry;
import dev.minceraft.sonus.common.protocol.tcp.holder.PmDataHolderBuf;
import dev.minceraft.sonus.common.version.Versioned;
import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.meta.AddCategorySvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.AddGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.CreateGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.JoinGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.JoinedGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.LeaveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStateSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStatesSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemoveCategorySvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemoveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemovePlayerStatePacket;
import dev.minceraft.sonus.svc.protocol.meta.RequestSecretSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.SecretSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.SvcMetaPacket;
import dev.minceraft.sonus.svc.protocol.meta.UpdateStateSvcPacket;
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

/*    public static final ContextedRegistry<PmDataHolderJsonObject, SvcMetaPacket<?>> JSON_REGISTRY =
            new ContextedRegistry.Builder<PmDataHolderJsonObject, SvcMetaPacket<?>>()
                    .codec((data, packet) -> packet.decode(data.getFirst()),
                            (data, packet) -> packet.encode(data.getFirst()))
                    .idCodec(holder -> PACKET_IDS.get((holder).getSecond()), (id, packet) -> {
                    })
                    .idMapper((id, sample) -> PACKET_IDS.put(sample.getPluginMessageChannel(), id))
                    .register(AddCategorySvcPacket.class, AddCategorySvcPacket::new)
                    .register(AddGroupSvcPacket.class, AddGroupSvcPacket::new)
                    .register(CreateGroupSvcPacket.class, CreateGroupSvcPacket::new)
                    .register(JoinGroupSvcPacket.class, JoinGroupSvcPacket::new)
                    .register(JoinedGroupSvcPacket.class, JoinedGroupSvcPacket::new)
                    .register(LeaveGroupSvcPacket.class, LeaveGroupSvcPacket::new)
                    .register(PlayerStateSvcPacket.class, PlayerStateSvcPacket::new)
                    .register(PlayerStateSvcPacket.class, PlayerStateSvcPacket::new)
                    .register(RemoveCategorySvcPacket.class, RemoveCategorySvcPacket::new)
                    .register(RemoveGroupSvcPacket.class, RemoveGroupSvcPacket::new)
                    .register(RequestSecretSvcPacket.class, RequestSecretSvcPacket::new)
                    .register(SecretSvcPacket.class, SecretSvcPacket::new)
                    .register(UpdateStateSvcPacket.class, UpdateStateSvcPacket::new)
                    .build();*/
}
