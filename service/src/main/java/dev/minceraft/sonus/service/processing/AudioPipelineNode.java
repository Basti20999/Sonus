package dev.minceraft.sonus.service.processing;

@FunctionalInterface
public interface AudioPipelineNode {

    short[] process(short[] data);
}
