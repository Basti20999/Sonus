package de.pianoman911.sonus.svcprotocol.meta;

import com.google.gson.JsonObject;
import de.pianoman911.sonus.svcprotocol.data.SonusVolumeCategory;
import de.pianoman911.sonus.svcprotocol.util.SvcPluginChannels;
import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ClientBound;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AddCategorySvcPacket extends SvcMetaPacket<AddCategorySvcPacket> implements ClientBound {

    private @MonotonicNonNull SonusVolumeCategory category;

    public AddCategorySvcPacket() {
        super(SvcPluginChannels.ADD_CATEGORY);
    }

    public AddCategorySvcPacket(SonusVolumeCategory category) {
        super(SvcPluginChannels.ADD_CATEGORY);
        this.category = category;
    }

    @Override
    public void encode(ByteBuf buf, int version) {
        this.category.encode(buf);
    }

    @Override
    public void decode(ByteBuf buf, int version) {
        this.category = new SonusVolumeCategory(buf);
    }

    @Override
    public void encode(@NotNull JsonObject json, int version) {
        this.category.encode(json);
    }

    @Override
    public void decode(JsonObject json) {
        this.category = new SonusVolumeCategory(json);
    }

    @Override
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handleAddCategoryPacket(player, this);
    }

    public SonusVolumeCategory getCategory() {
        return this.category;
    }

    public void setCategory(SonusVolumeCategory category) {
        this.category = category;
    }
}
