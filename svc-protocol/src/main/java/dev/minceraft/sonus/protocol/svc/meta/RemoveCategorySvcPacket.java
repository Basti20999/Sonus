package dev.minceraft.sonus.protocol.svc.meta;


import com.google.gson.JsonObject;
import dev.minceraft.sonus.protocol.svc.util.SvcPluginChannels;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RemoveCategorySvcPacket extends SvcMetaPacket<RemoveCategorySvcPacket> implements ClientBound {

    private @MonotonicNonNull String categoryId;

    public RemoveCategorySvcPacket() {
        super(SvcPluginChannels.REMOVE_CATEGORY);
    }

    @Override
    public void encode(ByteBuf buf) {
        Utf8String.write(buf, this.categoryId);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.categoryId = Utf8String.read(buf, 16);
    }

    @Override
    public void encode(JsonObject json, int version) {
        json.addProperty("categoryId", this.categoryId);
    }

    @Override
    public void decode(JsonObject json) {
        this.categoryId = json.get("categoryId").getAsString();
    }

    @Override
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handleRemoveCategoryPacket(player, this);
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
