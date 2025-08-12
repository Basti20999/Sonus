package dev.minceraft.sonus.svc.adapter.pipeline;

import dev.minceraft.sonus.common.protocol.udp.UdpBasedContext;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import io.netty.util.Recycler;

public class SvcUdpContext extends UdpBasedContext<SvcUdpContext> {

    public static final Recycler<SvcUdpContext> RECYCLER = createRecycler(SvcUdpContext::new);

    public SvcConnection connection;

    private SvcUdpContext(Recycler.Handle<SvcUdpContext> handle) {
        super(handle);
    }

    public static SvcUdpContext newInstance() {
        return RECYCLER.get();
    }

    @Override
    protected void clear() {
        super.clear();
        this.connection = null;
    }
}
