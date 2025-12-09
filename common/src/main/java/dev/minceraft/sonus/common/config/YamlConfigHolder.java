package dev.minceraft.sonus.common.config;

import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.function.Supplier;

@NullMarked
public class YamlConfigHolder<T> extends ConfigHolder<T, YamlConfigurationLoader> {

    private static final YamlConfigurationLoader.Builder CONFIG_BUILDER = YamlConfigurationLoader.builder()
            .nodeStyle(NodeStyle.BLOCK).indent(2);

    public YamlConfigHolder(Class<T> clazz, Supplier<T> def, Path path) {
        super(clazz, def, path, () -> {
            synchronized (CONFIG_BUILDER) {
                return CONFIG_BUILDER;
            }
        });
    }
}
