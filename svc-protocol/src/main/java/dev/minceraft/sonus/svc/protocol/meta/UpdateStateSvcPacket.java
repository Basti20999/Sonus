package dev.minceraft.sonus.svc.protocol.meta;

import com.google.gson.JsonObject;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class UpdateStateSvcPacket extends SvcMetaPacket<UpdateStateSvcPacket> implements ServerBound {

    private boolean disabled;

    public UpdateStateSvcPacket() {
        super(SvcPluginChannels.UPDATE_STATE);
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeBoolean(this.disabled);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.disabled = buf.readBoolean();
    }

    @Override
    public void encode(JsonObject json, int version) {
        json.addProperty("disabled", this.disabled);
    }

    @Override
    public void decode(JsonObject json) {
        this.disabled = json.get("disabled").getAsBoolean();
    }

    @Override
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handleUpdateStatePacket(player, this);
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
