package dev.minceraft.sonus.protocol.svc;


import dev.minceraft.sonus.protocol.svc.meta.IMetaSvcHandler;
import dev.minceraft.sonus.common.protocol.codec.IBufCodec;

public abstract class AbstractSvcPacket implements IBufCodec<IMetaSvcHandler> {

    public static final int COMPATIBILITY_VERSION = 18;

    protected AbstractSvcPacket() {
    }
}
