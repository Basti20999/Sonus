package dev.minceraft.sonus.svc.protocol.registries;

import dev.minceraft.sonus.common.protocol.registry.SimpleRegistry;
import dev.minceraft.sonus.common.protocol.tcp.holder.PmDataHolderBuf;
import dev.minceraft.sonus.common.protocol.tcp.holder.PmDataHolderJsonObject;
import dev.minceraft.sonus.svc.protocol.meta.AddCategorySvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.AddGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.CreateGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.JoinedGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.LeaveGroupSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.PlayerStateSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemoveCategorySvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.RemoveGroupSvcPacket;
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

    public static final SimpleRegistry<PmDataHolderBuf, SvcMetaPacket<?>> BUF_REGISTRY =
            new SimpleRegistry.Builder<PmDataHolderBuf, SvcMetaPacket<?>>()
                    .codec((data, packet) -> packet.decode(data.getFirst()),
                            (data, packet) -> packet.encode(data.getFirst()))
                    .idCodec(holder -> PACKET_IDS.get(((PmDataHolderBuf) holder).getSecond()), (id, packet) -> {
                    })
                    .idMapper((id, sample) -> PACKET_IDS.put(sample.getPluginMessageChannel(), id))
                    .register(AddCategorySvcPacket.class, AddCategorySvcPacket::new)
                    .register(AddGroupSvcPacket.class, AddGroupSvcPacket::new)
                    .register(CreateGroupSvcPacket.class, CreateGroupSvcPacket::new)
                    .register(JoinedGroupSvcPacket.class, JoinedGroupSvcPacket::new)
                    .register(JoinedGroupSvcPacket.class, JoinedGroupSvcPacket::new)
                    .register(LeaveGroupSvcPacket.class, LeaveGroupSvcPacket::new)
                    .register(PlayerStateSvcPacket.class, PlayerStateSvcPacket::new)
                    .register(PlayerStateSvcPacket.class, PlayerStateSvcPacket::new)
                    .register(RemoveCategorySvcPacket.class, RemoveCategorySvcPacket::new)
                    .register(RemoveGroupSvcPacket.class, RemoveGroupSvcPacket::new)
                    .register(RequestSecretSvcPacket.class, RequestSecretSvcPacket::new)
                    .register(SecretSvcPacket.class, SecretSvcPacket::new)
                    .register(UpdateStateSvcPacket.class, UpdateStateSvcPacket::new)
                    .build();

    public static final SimpleRegistry<PmDataHolderJsonObject, SvcMetaPacket<?>> JSON_REGISTRY =
            new SimpleRegistry.Builder<PmDataHolderJsonObject, SvcMetaPacket<?>>()
                    .codec((data, packet) -> packet.decode(data.getFirst()),
                            (data, packet) -> packet.encode(data.getFirst()))
                    .idCodec(holder -> PACKET_IDS.get(((PmDataHolderBuf) holder).getSecond()), (id, packet) -> {
                    })
                    .idMapper((id, sample) -> PACKET_IDS.put(sample.getPluginMessageChannel(), id))
                    .register(AddCategorySvcPacket.class, AddCategorySvcPacket::new)
                    .register(AddGroupSvcPacket.class, AddGroupSvcPacket::new)
                    .register(CreateGroupSvcPacket.class, CreateGroupSvcPacket::new)
                    .register(JoinedGroupSvcPacket.class, JoinedGroupSvcPacket::new)
                    .register(JoinedGroupSvcPacket.class, JoinedGroupSvcPacket::new)
                    .register(LeaveGroupSvcPacket.class, LeaveGroupSvcPacket::new)
                    .register(PlayerStateSvcPacket.class, PlayerStateSvcPacket::new)
                    .register(PlayerStateSvcPacket.class, PlayerStateSvcPacket::new)
                    .register(RemoveCategorySvcPacket.class, RemoveCategorySvcPacket::new)
                    .register(RemoveGroupSvcPacket.class, RemoveGroupSvcPacket::new)
                    .register(RequestSecretSvcPacket.class, RequestSecretSvcPacket::new)
                    .register(SecretSvcPacket.class, SecretSvcPacket::new)
                    .register(UpdateStateSvcPacket.class, UpdateStateSvcPacket::new)
                    .build();
}
