package dev.minceraft.sonus.svc.protocol;


import dev.minceraft.sonus.common.protocol.codec.IBufCodec;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class AbstractSvcPacket<H> implements IBufCodec<H> {

    public static final int COMPATIBILITY_VERSION = 18;

    protected AbstractSvcPacket() {
    }
}
