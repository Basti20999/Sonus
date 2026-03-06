package dev.minceraft.sonus.web.pion.ipc.sonusbound;
// Created by booky10 in Sonus (7:20 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.ipc.IpcMessage;
import dev.minceraft.sonus.web.pion.ipc.IpcTypes;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class IpcPeerError extends IpcMessage {

    private final String error;

    public IpcPeerError(ByteBuf buf) {
        this(buf.readInt(), IpcTypes.readUtf8(buf));
    }

    public IpcPeerError(int handlerId, String error) {
        super(handlerId);
        this.error = error;
    }

    @Override
    public void encode(ByteBuf buf) {
        super.encode(buf);
        IpcTypes.writeUtf8(buf, this.error);
    }

    public String getError() {
        return this.error;
    }
}
