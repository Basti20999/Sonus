package dev.minceraft.sonus.svc.protocol.meta;

import com.google.gson.JsonObject;
import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class UpdateStateSvcPacket extends SvcMetaPacket {

    private boolean disabled;

    public UpdateStateSvcPacket() {
        super(SvcPluginChannels.UPDATE_STATE);
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
        buf.writeBoolean(this.disabled);
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
        this.disabled = buf.readBoolean();
    }

    @Override
    public void encode(JsonObject json) {
        json.addProperty("disabled", this.disabled);
    }

    @Override
    public void decode(JsonObject json) {
        this.disabled = json.get("disabled").getAsBoolean();
    }

    @Override
    public void handle(IMetaSvcHandler handler) {
        handler.handleUpdateStatePacket(this);
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
