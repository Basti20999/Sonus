package dev.minceraft.sonus.common.protocol.tcp.holder;

import dev.minceraft.sonus.common.util.AbstractRecyclerPair;
import io.netty.buffer.ByteBuf;
import io.netty.util.Recycler;
import net.kyori.adventure.key.Key;

public class PmDataHolderBuf extends AbstractRecyclerPair<PmDataHolderBuf, ByteBuf, Key> {

    private static final Recycler<PmDataHolderBuf> PAIR_RECYCLER = createRecycler(PmDataHolderBuf::new);

    private PmDataHolderBuf(Recycler.Handle<PmDataHolderBuf> handle) {
        super(handle);
    }

    public static PmDataHolderBuf newInstance(ByteBuf buf, Key channel) {
        PmDataHolderBuf holder = PAIR_RECYCLER.get();
        holder.setFirst(buf);
        holder.setSecond(channel);
        return holder;
    }
}
