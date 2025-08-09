package de.pianoman911.sonus.svcprotocol.meta;

import com.google.gson.JsonObject;
import de.pianoman911.sonus.svcprotocol.data.SonusClientGroup;
import de.pianoman911.sonus.svcprotocol.util.SvcPluginChannels;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AddGroupSvcPacket extends SvcMetaPacket<AddGroupSvcPacket> implements ClientBound {

    private @MonotonicNonNull SonusClientGroup group;

    public AddGroupSvcPacket() {
        super(SvcPluginChannels.ADD_GROUP);
    }

    @Override
    public void encode(ByteBuf buf) {
        this.group.encode(buf);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.group = new SonusClientGroup(buf);
    }

    @Override
    public void encode(JsonObject json, int version) {
        this.group.encode(json);
    }

    @Override
    public void decode(JsonObject json) {
        this.group = new SonusClientGroup(json);
    }

    @Override
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handleAddGroupPacket(player, this);
    }

    public SonusClientGroup getGroup() {
        return this.group;
    }

    public void setGroup(SonusClientGroup group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "AddGroupSvcPacket{" +
                "group=" + group +
                '}';
    }
}
