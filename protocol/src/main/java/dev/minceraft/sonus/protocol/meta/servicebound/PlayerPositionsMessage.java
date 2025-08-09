package dev.minceraft.sonus.protocol.meta.servicebound;
// Created by booky10 in Sonus (01:15 17.07.2025)

import dev.minceraft.sonus.protocol.meta.IMetaHandler;
import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.data.WorldVec3d;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.UUID;

@NullMarked
public class PlayerPositionsMessage implements IMetaMessage {

    private @MonotonicNonNull Map<UUID, WorldVec3d> positions;

    public PlayerPositionsMessage() {
    }

    public PlayerPositionsMessage(Map<UUID, WorldVec3d> positions) {
        this.positions = positions;
    }

    @Override
    public void decode(ByteBuf buf, int version) {
        this.positions = DataTypeUtil.readMap(buf, DataTypeUtil::readUniqueId, WorldVec3d::read);
    }

    @Override
    public void encode(ByteBuf buf, int version) {
        DataTypeUtil.writeMap(buf, this.positions, DataTypeUtil::writeUniqueId, WorldVec3d::write);
    }

    @Override
    public void handle(IMetaHandler handler) {
        handler.handle(this);
    }

    public Map<UUID, WorldVec3d> getPositions() {
        return this.positions;
    }
}
