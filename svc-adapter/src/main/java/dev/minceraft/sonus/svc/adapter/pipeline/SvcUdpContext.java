package dev.minceraft.sonus.svc.adapter.pipeline;

import dev.minceraft.sonus.common.protocol.udp.UdpBasedContext;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import io.netty.util.Recycler;

import java.net.InetSocketAddress;

public class SvcUdpContext extends UdpBasedContext<SvcUdpContext> {

    public static final Recycler<SvcUdpContext> RECYCLER = createRecycler(SvcUdpContext::new);

    public SvcConnection connection;

    private SvcUdpContext(Recycler.Handle<SvcUdpContext> handle) {
        super(handle);
    }

    public static SvcUdpContext newInstance() {
        return RECYCLER.get();
    }

    public static SvcUdpContext newInstance(InetSocketAddress remoteAddress, SvcConnection connection) {
        SvcUdpContext context = newInstance();
        context.remoteAddress = remoteAddress;
        context.connection = connection;
        return context;
    }

    @Override
    protected void clear() {
        super.clear();
        this.connection = null;
    }
}
