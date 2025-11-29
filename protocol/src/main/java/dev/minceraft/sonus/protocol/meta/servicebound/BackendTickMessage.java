package dev.minceraft.sonus.protocol.meta.servicebound;
// Created by booky10 in Sonus (01:15 17.07.2025)

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import dev.minceraft.sonus.common.protocol.util.VarInt;
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
    private @Nullable Table<UUID, UUID, SonusPlayerState> perPlayerStates;
    private @Nullable Map<UUID, @Nullable String> teams;

    public BackendTickMessage() {
    }

    @Override
    public void decode(ByteBuf buf) {
        this.positions = DataTypeUtil.readNullable(buf, buffer ->
                DataTypeUtil.VAR_INT.readMap(buffer, DataTypeUtil::readUniqueId, WorldVec3d::read));
        this.perPlayerStates = DataTypeUtil.readNullable(buf, buffer -> {
            int rowCount = VarInt.read(buffer);
            Table<UUID, UUID, SonusPlayerState> perPlayerStates = HashBasedTable.create(rowCount, rowCount);
            for (int r = 0; r < rowCount; r++) {
                UUID playerId = DataTypeUtil.readUniqueId(buffer);
                int columnCount = VarInt.read(buffer);
                for (int c = 0; c < columnCount; c++) {
                    SonusPlayerState state = SonusPlayerState.read(buffer);
                    perPlayerStates.put(playerId, state.playerId(), state);
                }
            }
            return Tables.unmodifiableTable(perPlayerStates);
        });
        this.teams = DataTypeUtil.readNullable(buf, buffer ->
                DataTypeUtil.VAR_INT.readMap(buffer, DataTypeUtil::readUniqueId, buff ->
                        DataTypeUtil.readNullable(buff, Utf8String::read)));
    }

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeNullable(buf, this.positions, (buffer, positions) ->
                DataTypeUtil.VAR_INT.writeMap(buffer, positions, DataTypeUtil::writeUniqueId, WorldVec3d::write));
        DataTypeUtil.writeNullable(buf, this.perPlayerStates, (buffer, states) -> {
            Map<UUID, Map<UUID, SonusPlayerState>> rows = states.rowMap();
            VarInt.write(buffer, rows.size());
            for (Map.Entry<UUID, Map<UUID, SonusPlayerState>> row : rows.entrySet()) {
                DataTypeUtil.writeUniqueId(buffer, row.getKey());
                VarInt.write(buffer, row.getValue().size());
                for (SonusPlayerState state : row.getValue().values()) {
                    SonusPlayerState.write(buffer, state);
                }
            }
        });
        DataTypeUtil.writeNullable(buf, this.teams, (buffer, teams) ->
                DataTypeUtil.VAR_INT.writeMap(buffer, teams, DataTypeUtil::writeUniqueId, (buff, string) ->
                        DataTypeUtil.writeNullable(buff, string, Utf8String::write)));
    }

    @Override
    public void handle(IMetaHandler handler) {
        handler.handleBackendTick(this);
    }

    public @Nullable Map<UUID, WorldVec3d> getPositions() {
        return this.positions;
    }

    public void setPositions(@Nullable Map<UUID, WorldVec3d> positions) {
        this.positions = positions;
    }

    public @Nullable Table<UUID, UUID, SonusPlayerState> getPerPlayerStates() {
        return this.perPlayerStates;
    }

    public void setPerPlayerStates(@Nullable Table<UUID, UUID, SonusPlayerState> perPlayerStates) {
        this.perPlayerStates = perPlayerStates;
    }

    @Nullable
    public Map<UUID, @Nullable String> getTeams() {
        return this.teams;
    }

    public void setTeams(@Nullable Map<UUID, @Nullable String> teams) {
        this.teams = teams;
    }
}
