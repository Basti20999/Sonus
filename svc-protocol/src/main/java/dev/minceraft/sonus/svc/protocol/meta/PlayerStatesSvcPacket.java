package dev.minceraft.sonus.svc.protocol.meta;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.data.SvcPlayerState;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NullMarked
public class PlayerStatesSvcPacket extends SvcMetaPacket {

    private @MonotonicNonNull Map<UUID, SvcPlayerState> states;

    public PlayerStatesSvcPacket() {
        super(SvcPluginChannels.PLAYER_STATES);
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
        buf.writeInt(this.states.size());
        for (SvcPlayerState value : this.states.values()) {
            value.encode(buf);
        }
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
        int size = buf.readInt();
        this.states = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            SvcPlayerState state = new SvcPlayerState(buf);
            this.states.put(state.getPlayerId(), state);
        }
    }

    @Override
    public void encode(JsonObject json) {
        JsonArray array = new JsonArray(this.states.size());
        for (SvcPlayerState value : this.states.values()) {
            JsonObject object = new JsonObject();
            value.encode(object);
            array.add(object);
        }
        json.add("states", array);
    }

    @Override
    public void decode(JsonObject json) {
        JsonArray array = json.getAsJsonArray("states");
        this.states = new HashMap<>(array.size());
        for (JsonElement jsonElement : array) {
            SvcPlayerState state = new SvcPlayerState(jsonElement.getAsJsonObject());
            this.states.put(state.getPlayerId(), state);
        }
    }

    @Override
    public void handle(IMetaSvcHandler handler) {
        handler.handlePlayerStatesPacket(this);
    }

    public Map<UUID, SvcPlayerState> getStates() {
        return this.states;
    }

    public void setStates(Map<UUID, SvcPlayerState> states) {
        this.states = states;
    }
}
