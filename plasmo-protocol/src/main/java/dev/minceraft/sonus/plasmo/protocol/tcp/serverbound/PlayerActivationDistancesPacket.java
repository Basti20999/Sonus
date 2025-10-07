package dev.minceraft.sonus.plasmo.protocol.tcp.serverbound;

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPlasmoPacket;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class PlayerActivationDistancesPacket extends TcpPlasmoPacket<PlayerActivationDistancesPacket> {

    private @MonotonicNonNull Object2IntMap<UUID> distances;

    public PlayerActivationDistancesPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.INT.writeMap(buf, this.distances, DataTypeUtil::writeUniqueId, ByteBuf::writeInt);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.distances = DataTypeUtil.INT.readMap(buf, DataTypeUtil::readUniqueId, ByteBuf::readInt, Object2IntArrayMap::new);
    }

    @Override
    public void handle(TcpHandler handler) {
        handler.handlePlayerActivationDistancesPacket(this);
    }

    public Object2IntMap<UUID> getDistances() {
        return this.distances;
    }

    public void setDistances(Object2IntMap<UUID> distances) {
        this.distances = distances;
    }
}
