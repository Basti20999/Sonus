package dev.minceraft.sonus.svc.adapter;


import dev.minceraft.sonus.common.protocol.udp.AbstractUdpPipelineNode;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcUdpContext;
import dev.minceraft.sonus.svc.protocol.SvcUdpMagicCodec;

public abstract class SvcUdpPipelineNode<E, D> extends AbstractUdpPipelineNode<E, D, SvcUdpContext> {

    protected SvcUdpPipelineNode(SvcUdpMagicCodec svcCodec) {
        super(svcCodec);
    }
}
