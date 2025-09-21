package dev.minceraft.sonus.service.processing;

import java.util.List;

public class AudioPipeline implements AudioPipelineNode {

    private final List<AudioPipelineNode> nodes;

    public AudioPipeline(List<AudioPipelineNode> nodes) {
        this.nodes = nodes;
    }

    public void addNode(AudioPipelineNode node) {
        this.nodes.add(node);
    }


    @Override
    public short[] process(short[] data) {
        short[] output = data;
        for (AudioPipelineNode node : this.nodes) {
            output = node.process(output);
        }
        return output;
    }
}
