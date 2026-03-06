package dev.minceraft.sonus.web.pion.ipc.sonusbound;
// Created by booky10 in Sonus (6:34 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.ipc.IpcMessage;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class IpcApiAllocatePeerResp extends IpcMessage {

    public IpcApiAllocatePeerResp(ByteBuf buf) {
        super(buf.readInt());
    }

    public IpcApiAllocatePeerResp(int handlerId) {
        super(handlerId);
    }

    @Override
    public void encode(ByteBuf buf) {
        throw new UnsupportedOperationException();
    }
}
