package dev.minceraft.sonus.svc.protocol;

import dev.minceraft.sonus.common.protocol.codec.IBufCtxCodec;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class AbstractSvcPacket<H> implements IBufCtxCodec<H, SvcPacketContext> {

    protected AbstractSvcPacket() {
    }
}
