package dev.minceraft.sonus.web.protocol;

import dev.minceraft.sonus.common.protocol.codec.IBufCtxCodec;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class AbstractWebPacket<H> implements IBufCtxCodec<H, WsPacketContext> {

    protected AbstractWebPacket() {
    }
}
