package de.pianoman911.sonus.svcprotocol.meta;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.pianoman911.sonus.svcprotocol.data.SonusPlayerState;
import de.pianoman911.sonus.svcprotocol.util.SvcPluginChannels;
import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ClientBound;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NullMarked
public class PlayerStatesSvcPacket extends SvcMetaPacket<PlayerStatesSvcPacket> implements ClientBound {

    private @MonotonicNonNull Map<UUID, SonusPlayerState> states;

    public PlayerStatesSvcPacket() {
        super(SvcPluginChannels.PLAYER_STATES);
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(this.states.size());
        for (SonusPlayerState value : this.states.values()) {
            value.encode(buf);
        }
    }

    @Override
    public void decode(ByteBuf buf) {
        int size = buf.readInt();
        this.states = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            SonusPlayerState state = new SonusPlayerState(buf);
            this.states.put(state.getPlayerId(), state);
        }
    }

    @Override
    public void encode(JsonObject json, int version) {
        JsonArray array = new JsonArray(this.states.size());
        for (SonusPlayerState value : this.states.values()) {
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
            SonusPlayerState state = new SonusPlayerState(jsonElement.getAsJsonObject());
            this.states.put(state.getPlayerId(), state);
        }
    }

    @Override
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handlePlayerStatesPacket(player, this);
    }

    public Map<UUID, SonusPlayerState> getStates() {
        return this.states;
    }

    public void setStates(Map<UUID, SonusPlayerState> states) {
        this.states = states;
    }
}
