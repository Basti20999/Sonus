package dev.minceraft.sonus.common.rooms.options;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public final class RoomDefinition {

    private final Table<UUID, UUID, RelationState> staticOverrides = HashBasedTable.create();
    private final Table<String,String, RelationState> teamOverrides = HashBasedTable.create();
    private RelationState defaultState = RelationState.SPATIAL;
    private boolean dirty = true;

    public RoomDefinition setStatic(UUID sender, UUID receiver, @Nullable RelationState state) {
        if (state != null) {
            this.staticOverrides.put(sender, receiver, state);
        } else {
            this.staticOverrides.remove(sender, receiver);
        }
        this.markDirty();
        return this;
    }

    public RoomDefinition setTeam(String senderTeam,String receiverTeam, @Nullable RelationState state) {
        if (state != null) {
            this.teamOverrides.put(senderTeam, receiverTeam,null);
        } else {
            this.teamOverrides.remove(senderTeam,receiverTeam);
        }
        this.markDirty();
        return this;
    }

    public RoomDefinition setDefault(RelationState state) {
        this.defaultState = state;
        return this;
    }

    private void markDirty() {
        this.dirty = true;
    }

    public RelationState getState(IAudioSource sender, ISonusPlayer receiver) {
        // check for static relation
        RelationState staticState = this.staticOverrides.get(sender.getSenderId(), receiver.getUniqueId());
        if (staticState != null) {
            return staticState;
        }
        // check for team relation
        if (sender instanceof ISonusPlayer playerSender) {
            RelationState teamState = this.teamOverrides.get(playerSender.getTeam(), receiver.getTeam());
            if (teamState != null) {
                return teamState;
            }
        }
        // return default
        return this.defaultState;
    }

    public enum RelationState {
        STATIC,
        SPATIAL,
        SPATIAL_NORMALIZED,
        HIDE,
    }
}
