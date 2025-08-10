package dev.minceraft.sonus.svc.protocol.registries;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import dev.minceraft.sonus.common.protocol.registry.SimpleRegistry;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public final class SvcMetaPacketRegistry {

    private static final Gson GSON = new Gson();

    private static final Map<Key, Integer> PACKET_IDS = new HashMap<>();

    public static final SimpleRegistry<DataHolder<ByteBuf>, SvcMetaPacket<?>> BUF_REGISTRY =
            new SimpleRegistry.Builder<DataHolder<ByteBuf>, SvcMetaPacket<?>>()
                    .codec((data, packet) -> packet.decode(data.data()),
                            (data, packet) -> packet.encode(data.data()))
                    .idCodec(buf -> PACKET_IDS.get(((DataHolder<?>) buf).channel()), (id, packet) -> {
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

    public static final SimpleRegistry<DataHolder<JsonObject>, SvcMetaPacket<?>> JSON_REGISTRY =
            new SimpleRegistry.Builder<DataHolder<JsonObject>, SvcMetaPacket<?>>()
                    .codec((data, packet) -> packet.decode(data.data()),
                            (data, packet) -> packet.encode(data.data()))
                    .idCodec(buf -> PACKET_IDS.get(((DataHolder<?>) buf).channel()), (id, packet) -> {
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

    public record DataHolder<D>(D data, Key channel) {}
}
