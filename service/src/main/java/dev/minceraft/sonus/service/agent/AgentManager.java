package dev.minceraft.sonus.service.agent;

import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.protocol.meta.SonusAgentPmCodec;
import dev.minceraft.sonus.service.SonusService;
import net.kyori.adventure.key.Key;

import java.util.Set;

public class AgentManager {

    private final SonusService service;
    private final AgentListener listener;
    private final SonusAgentPmCodec codec;

    public AgentManager(SonusService service) {
        this.service = service;
        this.listener = new AgentListener(service);
        this.codec = new SonusAgentPmCodec(Set.of(Key.key(SonusConstants.PLUGIN_MESSAGE_CHANNEL)), this.listener);
    }

    public void init() {
        this.service.getPluginMessenger().registerCodec(this.codec);
    }
}
