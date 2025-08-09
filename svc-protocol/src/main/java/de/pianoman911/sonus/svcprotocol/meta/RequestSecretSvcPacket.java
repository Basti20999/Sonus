package de.pianoman911.sonus.svcprotocol.meta;


import com.google.gson.JsonObject;
import de.pianoman911.sonus.svcprotocol.util.SvcPluginChannels;
import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ServerBound;
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
