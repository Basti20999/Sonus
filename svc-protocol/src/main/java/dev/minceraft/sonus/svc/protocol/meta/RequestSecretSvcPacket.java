package dev.minceraft.sonus.svc.protocol.meta;


import com.google.gson.JsonObject;
import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RequestSecretSvcPacket extends SvcMetaPacket {

    private int compatibilityVersion;

    public RequestSecretSvcPacket() {
        super(SvcPluginChannels.REQUEST_SECRET);
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
        buf.writeInt(this.compatibilityVersion);
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
        this.compatibilityVersion = buf.readInt();
    }

    @Override
    public void encode(JsonObject json) {
        json.addProperty("compatibilityVersion", this.compatibilityVersion);
    }

    @Override
    public void decode(JsonObject json) {
        this.compatibilityVersion = json.get("compatibilityVersion").getAsInt();
    }

    @Override
    public void handle(IMetaSvcHandler handler) {
        handler.handleRequestSecretPacket( this);
    }

    public int getCompatibilityVersion() {
        return this.compatibilityVersion;
    }

    public void setCompatibilityVersion(int compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }
}
