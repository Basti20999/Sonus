package dev.minceraft.sonus.plasmo.protocol;

import dev.minceraft.sonus.common.protocol.codec.IBufCodec;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class AbstractPlasmoPacket<H> implements IBufCodec<H> {

    protected AbstractPlasmoPacket() {
    }
}
