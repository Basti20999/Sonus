package dev.minceraft.sonus.svc.adapter;


import dev.minceraft.sonus.common.protocol.udp.AbstractUdpPipelineNode;
import dev.minceraft.sonus.svc.protocol.SvcUdpMagicCodec;

public abstract class SvcUdpPipelineNode<I, O> extends AbstractUdpPipelineNode<I, O> {

    protected SvcUdpPipelineNode() {
        super(SvcUdpMagicCodec.INSTANCE);
    }
}
