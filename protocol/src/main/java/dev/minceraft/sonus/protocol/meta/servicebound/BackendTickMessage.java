package dev.minceraft.sonus.protocol.meta.servicebound;
// Created by booky10 in Sonus (01:15 17.07.2025)

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.protocol.meta.IMetaHandler;
import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@NullMarked
public class BackendTickMessage implements IMetaMessage {

    private @Nullable Map<UUID, WorldVec3d> positions;
    private @Nullable Multimap<UUID, SonusPlayerState> perPlayerStates;

    public BackendTickMessage() {
    }

    @Override
    public void decode(ByteBuf buf) {
        this.positions = DataTypeUtil.readIf(buf, buffer ->
                DataTypeUtil.readMap(buffer, DataTypeUtil::readUniqueId, WorldVec3d::read));
        this.perPlayerStates = DataTypeUtil.readIf(buf, buffer ->
                DataTypeUtil.readMultiMap(buffer, DataTypeUtil::readUniqueId, SonusPlayerState::read, HashMultimap::create));
    }

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeNullable(buf, this.positions, (buffer, positions) ->
                DataTypeUtil.writeMap(buffer, positions, DataTypeUtil::writeUniqueId, WorldVec3d::write));
        DataTypeUtil.writeNullable(buf, this.perPlayerStates, (buffer, states) ->
                DataTypeUtil.writeMultiMap(buffer, states, DataTypeUtil::writeUniqueId, SonusPlayerState::write));
    }

    @Override
    public void handle(IMetaHandler handler) {
        handler.handle(this);
    }

    @Nullable
    public Map<UUID, WorldVec3d> getPositions() {
        return this.positions;
    }

    public void setPositions(@Nullable Map<UUID, WorldVec3d> positions) {
        this.positions = positions;
    }

    @Nullable
    public Multimap<UUID, SonusPlayerState> getPerPlayerStates() {
        return this.perPlayerStates;
    }

    public void setPerPlayerStates(@Nullable Multimap<UUID, SonusPlayerState> perPlayerStates) {
        this.perPlayerStates = perPlayerStates;
    }
}
