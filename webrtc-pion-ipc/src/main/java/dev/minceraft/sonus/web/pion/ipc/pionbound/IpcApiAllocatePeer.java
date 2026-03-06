package dev.minceraft.sonus.web.pion.ipc.pionbound;
// Created by booky10 in Sonus (6:30 PM 06.03.2026)

import dev.minceraft.sonus.common.protocol.util.Utf8String;
import dev.minceraft.sonus.web.pion.PionApi;
import dev.minceraft.sonus.web.pion.PionApi.BundlePolicy;
import dev.minceraft.sonus.web.pion.ipc.IpcMessage;
import dev.minceraft.sonus.web.pion.ipc.IpcTypes;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class IpcApiAllocatePeer extends IpcMessage {

    private final List<PionApi.IceServer> iceServers;
    private final BundlePolicy bundlePolicy;
    private final String id;

    public IpcApiAllocatePeer(int handlerId, List<PionApi.IceServer> iceServers, BundlePolicy bundlePolicy, String id) {
        super(handlerId);
        this.iceServers = iceServers;
        this.bundlePolicy = bundlePolicy;
        this.id = id;
    }

    @Override
    public void encode(ByteBuf buf) {
        super.encode(buf);
        IpcTypes.writeCollection(buf, this.iceServers, IpcTypes::writeIceServer);
        IpcTypes.writeEnum(buf, this.bundlePolicy);
        Utf8String.write(buf, this.id);
    }

    public List<PionApi.IceServer> getIceServers() {
        return this.iceServers;
    }

    public BundlePolicy getBundlePolicy() {
        return this.bundlePolicy;
    }

    public String getId() {
        return this.id;
    }
}
