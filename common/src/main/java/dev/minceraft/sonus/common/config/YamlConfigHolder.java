package dev.minceraft.sonus.common.config;

import dev.minceraft.sonus.common.config.serializer.AddressSerializer;
import dev.minceraft.sonus.common.config.serializer.RoomDefinitionSerializer;
import dev.minceraft.sonus.common.rooms.options.RoomDefinition;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Supplier;

@NullMarked
public class YamlConfigHolder<T> {

    private static final YamlConfigurationLoader.Builder CONFIG_BUILDER = YamlConfigurationLoader.builder()
            .nodeStyle(NodeStyle.BLOCK).indent(2);

    private final Class<T> clazz;
    private final Supplier<T> def;
    private final Path path;
    private final YamlConfigurationLoader loader;
    private final Set<Consumer<T>> reloadHooks = new CopyOnWriteArraySet<>();
    private T config;

    public YamlConfigHolder(Class<T> clazz, Supplier<T> def, Path path) {
        this.clazz = clazz;
        this.path = path;
        this.def = def;

        synchronized (CONFIG_BUILDER) {
            this.loader = CONFIG_BUILDER
                    .defaultOptions(opts -> opts.serializers(builder -> builder
                            .register(InetSocketAddress.class, AddressSerializer.INSTANCE)
                            .register(RoomDefinition.class, RoomDefinitionSerializer.INSTANCE)
                    ))
                    .path(path)
                    .build();
        }

        // initialize config
        this.config = this.reloadConfig();
    }

    public void addReloadHook(Consumer<T> consumer) {
        this.reloadHooks.add(consumer);
    }

    public void addReloadHookAndRun(Consumer<T> consumer) {
        this.addReloadHook(consumer);
        consumer.accept(this.config);
    }

    public T reloadConfig() {
        try {
            synchronized (this) {
                if (Files.exists(this.path)) {
                    T config = this.loader.load().get(this.clazz);
                    this.config = Objects.requireNonNull(config);
                } else {
                    // create default
                    this.config = this.def.get();
                }
                this.saveConfig0();
                return this.config;
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load config", exception);
        } finally {
            // trigger reload hooks
            for (Consumer<T> runnable : this.reloadHooks) {
                runnable.accept(this.config);
            }
        }
    }

    public void saveConfig() {
        synchronized (this) {
            this.saveConfig0();
        }
    }

    private void saveConfig0() {
        try {
            Files.createDirectories(this.path.getParent());
            this.loader.save(this.loader.createNode().set(this.clazz, this.config));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public T getDelegate() {
        return this.config;
    }
}
