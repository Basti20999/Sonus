package dev.minceraft.sonus.svc.protocol.meta;


import com.google.gson.JsonObject;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RequestSecretSvcPacket extends SvcMetaPacket<RequestSecretSvcPacket> implements ServerBound {

    private int compatibilityVersion;

    public RequestSecretSvcPacket() {
        super(SvcPluginChannels.REQUEST_SECRET);
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(this.compatibilityVersion);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.compatibilityVersion = buf.readInt();
    }

    @Override
    public void encode(JsonObject json, int version) {
        json.addProperty("compatibilityVersion", this.compatibilityVersion);
    }

    @Override
    public void decode(JsonObject json) {
        this.compatibilityVersion = json.get("compatibilityVersion").getAsInt();
    }

    @Override
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handleRequestSecretPacket(player, this);
    }

    public int getCompatibilityVersion() {
        return this.compatibilityVersion;
    }

    public void setCompatibilityVersion(int compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }
}
