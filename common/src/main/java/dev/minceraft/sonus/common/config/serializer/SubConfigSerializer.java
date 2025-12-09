package dev.minceraft.sonus.common.config.serializer;

import dev.minceraft.sonus.common.config.ISubConfig;
import dev.minceraft.sonus.common.config.SubConfigSection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@NullMarked
public final class SubConfigSerializer implements TypeSerializer<SubConfigSection> {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");
    private final Map<String, ISubConfig> defaultAdapterConfigs = new HashMap<>();

    @Override
    public SubConfigSection deserialize(Type type, ConfigurationNode node) throws SerializationException {
        Map<Class<? extends ISubConfig>, ISubConfig> adapterConfigs = new HashMap<>();
        if (node.virtual()) {
            for (Map.Entry<String, ISubConfig> entry : this.defaultAdapterConfigs.entrySet()) {
                adapterConfigs.put(entry.getValue().getClass(), entry.getValue());
            }
            return new SubConfigSection(adapterConfigs);
        }
        for (ConfigurationNode nodes : node.childrenList()) {
            if (nodes.virtual()) {
                continue;
            }
            String entry = nodes.key().toString();
            ISubConfig subConfig = this.defaultAdapterConfigs.get(entry);
            if (subConfig == null) {
                LOGGER.warn("No default adapter config found for '{}', skipping...", entry);
                continue;
            }

            adapterConfigs.put(subConfig.getClass(), nodes.get(subConfig.getClass()));
        }

        return new SubConfigSection(adapterConfigs);
    }

    @Override
    public void serialize(Type type, @Nullable SubConfigSection obj, ConfigurationNode node) throws SerializationException {
        for (Map.Entry<Class<? extends ISubConfig>, ISubConfig> entry : obj.getAdapterConfigs().entrySet()) {
            ConfigurationNode subNode = node.appendListNode();
            subNode.node(this.getKeyForConfig(entry.getValue())).set(entry.getValue());
        }
    }

    public void registerDefaultAdapterConfig(ISubConfig config) {
        LOGGER.info("Registering default adapter config for {}", config.getClass().getName());
        this.defaultAdapterConfigs.put(this.getKeyForConfig(config), config);
    }

    private String getKeyForConfig(ISubConfig config) {
        return config.getClass().getName();
    }
}
