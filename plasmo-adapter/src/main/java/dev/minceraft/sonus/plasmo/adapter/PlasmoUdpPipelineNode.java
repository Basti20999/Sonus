package dev.minceraft.sonus.plasmo.adapter;

import dev.minceraft.sonus.common.protocol.udp.AbstractUdpPipelineNode;
import dev.minceraft.sonus.plasmo.adapter.pipeline.PlasmoUdpContext;
import dev.minceraft.sonus.plasmo.protocol.PlasmoUdpMagicCodec;

public abstract class PlasmoUdpPipelineNode<E, D> extends AbstractUdpPipelineNode<E, D, PlasmoUdpContext> {

    protected PlasmoUdpPipelineNode(PlasmoUdpMagicCodec plasmoCodec) {
        super(plasmoCodec);
    }
}
