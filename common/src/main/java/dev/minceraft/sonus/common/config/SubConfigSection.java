package dev.minceraft.sonus.common.config;

import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public class SubConfigSection {

    private final Map<Class<? extends ISubConfig>, ISubConfig> adapterConfigs;

    public SubConfigSection() {
        this(new HashMap<>());
    }

    public SubConfigSection(Map<Class<? extends ISubConfig>, ISubConfig> adapterConfigs) {
        this.adapterConfigs = adapterConfigs;
    }

    public <T extends ISubConfig> T getAdapterConfig(Class<T> adapterConfigClass) {
        return adapterConfigClass.cast(this.adapterConfigs.get(adapterConfigClass));
    }

    public Map<Class<? extends ISubConfig>, ISubConfig> getAdapterConfigs() {
        return this.adapterConfigs;
    }
}
