package dev.minceraft.sonus.svc.protocol;


import dev.minceraft.sonus.svc.protocol.meta.IMetaSvcHandler;
import dev.minceraft.sonus.common.protocol.codec.IBufCodec;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class AbstractSvcPacket implements IBufCodec<IMetaSvcHandler> {

    public static final int COMPATIBILITY_VERSION = 18;

    protected AbstractSvcPacket() {
    }
}
