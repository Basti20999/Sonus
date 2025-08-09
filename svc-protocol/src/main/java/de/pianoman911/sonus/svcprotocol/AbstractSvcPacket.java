package de.pianoman911.sonus.svcprotocol;


import de.pianoman911.sonus.svcprotocol.meta.IMetaSvcHandler;
import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.IBufCodec;
import dev.minecraft.sonus.common.protocol.codec.IPacket;

public abstract class AbstractSvcPacket implements IBufCodec<ISonusPlayer, IMetaSvcHandler>, IPacket {

    public static final int COMPATIBILITY_VERSION = 18;

    protected AbstractSvcPacket() {
    }
}
