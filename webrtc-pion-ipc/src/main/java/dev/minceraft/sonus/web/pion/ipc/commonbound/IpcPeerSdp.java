package dev.minceraft.sonus.web.pion.ipc.commonbound;
// Created by booky10 in Sonus (7:03 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.ipc.IpcMessage;
import dev.minceraft.sonus.web.pion.ipc.IpcTypes;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class IpcPeerSdp extends IpcMessage {

    private final String sdp;

    public IpcPeerSdp(ByteBuf buf) {
        this(buf.readInt(), IpcTypes.readUtf8(buf));
    }

    public IpcPeerSdp(int handlerId, String sdp) {
        super(handlerId);
        this.sdp = sdp;
    }

    @Override
    public void encode(ByteBuf buf) {
        super.encode(buf);
        IpcTypes.writeUtf8(buf, this.sdp);
    }

    public String getSdp() {
        return this.sdp;
    }
}
