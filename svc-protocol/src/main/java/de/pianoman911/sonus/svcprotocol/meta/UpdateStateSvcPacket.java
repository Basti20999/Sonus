package de.pianoman911.sonus.svcprotocol.meta;

import com.google.gson.JsonObject;
import de.pianoman911.sonus.svcprotocol.util.SvcPluginChannels;
import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ServerBound;
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
