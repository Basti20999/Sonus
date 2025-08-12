package dev.minceraft.sonus.common.adapter;
// Created by booky10 in Sonus (01:41 10.08.2025)

import dev.minceraft.sonus.common.protocol.udp.UdpBasedContext;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface VoiceProtocolAdapter {

    byte getMagicByte();

    UdpBasedContext<?> newPipelineContext();
}
