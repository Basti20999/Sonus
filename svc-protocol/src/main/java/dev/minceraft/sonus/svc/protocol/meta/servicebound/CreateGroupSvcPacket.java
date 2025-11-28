package dev.minceraft.sonus.svc.protocol.meta.servicebound;

import com.google.gson.JsonObject;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.data.SonusGroupType;
import dev.minceraft.sonus.svc.protocol.meta.IMetaSvcHandler;
import dev.minceraft.sonus.svc.protocol.meta.SvcMetaPacket;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CreateGroupSvcPacket extends SvcMetaPacket {

    private @MonotonicNonNull String name;
    private @Nullable String password;
    private @MonotonicNonNull SonusGroupType type;

    public CreateGroupSvcPacket() {
        super(SvcPluginChannels.CREATE_GROUP);
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
        Utf8String.write(buf, this.name);
        DataTypeUtil.writeNullable(buf, this.password, Utf8String::write);
        buf.writeShort(this.type.ordinal());
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
        this.name = Utf8String.read(buf, 512);
        this.password = DataTypeUtil.readNullable(buf, b -> Utf8String.read(b, 512));
        this.type = SonusGroupType.values()[buf.readShort()];
    }

    @Override
    public void encode(JsonObject json) {
        json.addProperty("name", this.name);
        if (this.password != null) {
            json.addProperty("password", this.password);
        }
        json.addProperty("type", this.type.getId());
    }

    @Override
    public void decode(JsonObject json) {
        this.name = json.get("name").getAsString();
        this.password = json.has("password") ?
                json.get("password").getAsString() : null;
        this.type = SonusGroupType.ID_INDEX.valueOrThrow(
                json.get("type").getAsString());
    }

    @Override
    public void handle(IMetaSvcHandler handler) {
        handler.handleCreateGroupPacket(this);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getPassword() {
        return this.password;
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    public SonusGroupType getType() {
        return this.type;
    }

    public void setType(SonusGroupType type) {
        this.type = type;
    }
}
