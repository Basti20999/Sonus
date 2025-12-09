package dev.minceraft.sonus.common.config;

import dev.minceraft.sonus.common.config.serializer.AddressSerializer;
import dev.minceraft.sonus.common.config.serializer.RoomDefinitionSerializer;
import dev.minceraft.sonus.common.config.serializer.SubConfigSerializer;
import dev.minceraft.sonus.common.rooms.options.RoomDefinition;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigHolder<T, L extends AbstractConfigurationLoader<?>> {

    private final Class<T> clazz;
    private final Supplier<T> def;
    private final Path path;
    private final L loader;
    private final Set<Consumer<T>> reloadHooks = new CopyOnWriteArraySet<>();
    private final SubConfigSerializer subConfigSerializer = new SubConfigSerializer();

    private T config;

    public <B extends AbstractConfigurationLoader.Builder<B, L>> ConfigHolder(Class<T> clazz, Supplier<T> def, Path path,
                                                                              Supplier<B> loaderBuilder) {
        this.clazz = clazz;
        this.path = path;
        this.def = def;
        this.loader = loaderBuilder.get().defaultOptions(opts ->
                        opts.serializers(builder -> builder
                                .register(InetSocketAddress.class, AddressSerializer.INSTANCE)
                                .register(RoomDefinition.class, RoomDefinitionSerializer.INSTANCE)
                                .register(SubConfigSection.class, this.subConfigSerializer)
                        ))
                .path(path)
                .build();

    }

    public void addReloadHook(Consumer<T> consumer) {
        this.reloadHooks.add(consumer);
    }

    public void addReloadHookAndRun(Consumer<T> consumer) {
        this.addReloadHook(consumer);

        if (this.config != null) {
            consumer.accept(this.config);
        }
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

    public void registerDefaultConfig(ISubConfig defaultConfig) {
        this.subConfigSerializer.registerDefaultAdapterConfig(defaultConfig);
    }
}
