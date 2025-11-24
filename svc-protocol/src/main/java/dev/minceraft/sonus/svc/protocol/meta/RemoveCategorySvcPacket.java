package dev.minceraft.sonus.svc.protocol.meta;


import com.google.gson.JsonObject;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RemoveCategorySvcPacket extends SvcMetaPacket<RemoveCategorySvcPacket>{

    private @MonotonicNonNull String categoryId;

    public RemoveCategorySvcPacket() {
        super(SvcPluginChannels.REMOVE_CATEGORY);
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
        Utf8String.write(buf, this.categoryId);
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
        this.categoryId = Utf8String.read(buf, 16);
    }

    @Override
    public void encode(JsonObject json) {
        json.addProperty("categoryId", this.categoryId);
    }

    @Override
    public void decode(JsonObject json) {
        this.categoryId = json.get("categoryId").getAsString();
    }

    @Override
    public void handle(IMetaSvcHandler handler) {
        handler.handleRemoveCategoryPacket(this);
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
