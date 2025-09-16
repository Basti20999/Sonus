package dev.minceraft.sonus.svc.protocol;


import dev.minceraft.sonus.common.protocol.codec.IBufCodec;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class AbstractSvcPacket<H> implements IBufCodec<H> {

    protected AbstractSvcPacket() {
    }
}
