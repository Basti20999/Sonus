package de.pianoman911.sonus.svcprotocol;


import de.pianoman911.sonus.svcprotocol.meta.IMetaSvcHandler;
import dev.minceraft.sonus.common.protocol.codec.IBufCodec;

public abstract class AbstractSvcPacket implements IBufCodec<IMetaSvcHandler> {

    public static final int COMPATIBILITY_VERSION = 18;

    protected AbstractSvcPacket() {
    }
}
