package dev.minceraft.sonus.web.pion.ipc.pionbound;
// Created by booky10 in Sonus (7:03 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.ipc.IpcMessage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class IpcPeerClose extends IpcMessage {

    public IpcPeerClose(int handlerId) {
        super(handlerId);
    }
}
