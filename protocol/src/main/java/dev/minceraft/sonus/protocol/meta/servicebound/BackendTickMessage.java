package dev.minceraft.sonus.protocol.meta.servicebound;
// Created by booky10 in Sonus (01:15 17.07.2025)

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
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
    private @Nullable Map<UUID, @Nullable String> teams;

    public BackendTickMessage() {
    }

    @Override
    public void decode(ByteBuf buf) {
        this.positions = DataTypeUtil.readIf(buf, buffer ->
                DataTypeUtil.VAR_INT.readMap(buffer, DataTypeUtil::readUniqueId, WorldVec3d::read));
        this.perPlayerStates = DataTypeUtil.readIf(buf, buffer ->
                DataTypeUtil.VAR_INT.readMultiMap(buffer, DataTypeUtil::readUniqueId, SonusPlayerState::read, HashMultimap::create));
        this.teams = DataTypeUtil.readIf(buf, buffer ->
                DataTypeUtil.VAR_INT.readMap(buffer, DataTypeUtil::readUniqueId, buff ->
                        DataTypeUtil.readIf(buff, Utf8String::read)));
    }

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeNullable(buf, this.positions, (buffer, positions) ->
                DataTypeUtil.VAR_INT.writeMap(buffer, positions, DataTypeUtil::writeUniqueId, WorldVec3d::write));
        DataTypeUtil.writeNullable(buf, this.perPlayerStates, (buffer, states) ->
                DataTypeUtil.VAR_INT.writeMultiMap(buffer, states, DataTypeUtil::writeUniqueId, SonusPlayerState::write));
        DataTypeUtil.writeNullable(buf, this.teams, (buffer, teams) ->
                DataTypeUtil.VAR_INT.writeMap(buffer, teams, DataTypeUtil::writeUniqueId, (buff, string) ->
                        DataTypeUtil.writeNullable(buff, string, Utf8String::write)));
    }

    @Override
    public void handle(IMetaHandler handler) {
        handler.handleBackendTick(this);
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

    @Nullable
    public Map<UUID, @Nullable String> getTeams() {
        return this.teams;
    }

    public void setTeams(@Nullable Map<UUID, String> teams) {
        this.teams = teams;
    }
}
