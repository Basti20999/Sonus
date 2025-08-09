package de.pianoman911.sonus.svcprotocol.meta;

import com.google.gson.JsonObject;
import de.pianoman911.sonus.svcprotocol.data.SonusGroupType;
import de.pianoman911.sonus.svcprotocol.util.SvcPluginChannels;
import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ServerBound;
import dev.minecraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minecraft.sonus.common.protocol.util.Utf8String;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CreateGroupSvcPacket extends SvcMetaPacket<CreateGroupSvcPacket> implements ServerBound {

    private @MonotonicNonNull String name;
    private @Nullable String password;
    private @MonotonicNonNull SonusGroupType type;

    public CreateGroupSvcPacket() {
        super(SvcPluginChannels.CREATE_GROUP);
    }

    @Override
    public void encode(ByteBuf buf) {
        Utf8String.write(buf, this.name);
        DataTypeUtil.writeNullable(buf, this.password, Utf8String::write);
        buf.writeShort(this.type.ordinal());
    }

    @Override
    public void decode(ByteBuf buf) {
        this.name = Utf8String.read(buf, 512);
        this.password = DataTypeUtil.readIf(buf, b -> Utf8String.read(b, 512));
        this.type = SonusGroupType.values()[buf.readShort()];
    }

    @Override
    public void encode(JsonObject json, int version) {
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
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handleCreateGroupPacket(player, this);
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
