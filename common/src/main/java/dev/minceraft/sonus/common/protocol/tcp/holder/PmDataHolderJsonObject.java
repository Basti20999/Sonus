package dev.minceraft.sonus.common.protocol.tcp.holder;

import com.google.gson.JsonObject;
import dev.minceraft.sonus.common.util.AbstractRecyclerPair;
import io.netty.util.Recycler;
import net.kyori.adventure.key.Key;

public class PmDataHolderJsonObject extends AbstractRecyclerPair<PmDataHolderJsonObject, JsonObject, Key> {

    private static final Recycler<PmDataHolderJsonObject> PAIR_RECYCLER = createRecycler(PmDataHolderJsonObject::new);

    private PmDataHolderJsonObject(Recycler.Handle<PmDataHolderJsonObject> handle) {
        super(handle);
    }

    public static PmDataHolderJsonObject newInstance(JsonObject buf, Key channel) {
        PmDataHolderJsonObject holder = PAIR_RECYCLER.get();
        holder.setFirst(buf);
        holder.setSecond(channel);
        return holder;
    }
}
