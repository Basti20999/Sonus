package dev.minceraft.sonus.web.pion.ipc;
// Created by booky10 in Sonus (6:19 PM 06.03.2026)

import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class IpcMessage {

    protected final int handlerId;

    public IpcMessage(int handlerId) {
        this.handlerId = handlerId;
    }

    public void encode(ByteBuf buf) {
        buf.writeInt(this.handlerId);
    }

    public int getHandlerId() {
        return this.handlerId;
    }
}
