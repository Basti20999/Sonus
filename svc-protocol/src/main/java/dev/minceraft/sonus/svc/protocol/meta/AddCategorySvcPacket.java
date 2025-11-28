package dev.minceraft.sonus.svc.protocol.meta;

import com.google.gson.JsonObject;
import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.data.SonusVolumeCategory;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AddCategorySvcPacket extends SvcMetaPacket {

    private @MonotonicNonNull SonusVolumeCategory category;

    public AddCategorySvcPacket() {
        super(SvcPluginChannels.ADD_CATEGORY);
    }

    public AddCategorySvcPacket(SonusVolumeCategory category) {
        super(SvcPluginChannels.ADD_CATEGORY);
        this.category = category;
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
        this.category.encode(buf, ctx);
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
        this.category = new SonusVolumeCategory(buf, ctx);
    }

    @Override
    public void encode(JsonObject json) {
        this.category.encode(json);
    }

    @Override
    public void decode(JsonObject json) {
        this.category = new SonusVolumeCategory(json);
    }

    @Override
    public void handle(IMetaSvcHandler handler) {
        handler.handleAddCategoryPacket(this);
    }

    public SonusVolumeCategory getCategory() {
        return this.category;
    }

    public void setCategory(SonusVolumeCategory category) {
        this.category = category;
    }
}
