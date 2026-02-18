package dev.minceraft.sonus.service.velocity;
// Created by booky10 in Sonus (02:43 06.12.2025)

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import org.jspecify.annotations.NullMarked;

import java.util.Locale;
import java.util.ResourceBundle;

import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.translation.GlobalTranslator.translator;

@NullMarked
@Singleton
public class SonusTranslationLoader {

    private static final String NAMESPACE = "sonus";

    private final MiniMessageTranslationStore store;

    @Inject
    public SonusTranslationLoader() {
        this.store = MiniMessageTranslationStore.create(key(NAMESPACE, "i18n"));
    }

    public void load(Locale locale) {
        ClassLoader classloader = this.getClass().getClassLoader();
        ResourceBundle bundle = ResourceBundle.getBundle(NAMESPACE, locale, classloader);
        this.store.registerAll(locale, bundle.keySet(), bundle::getString);
    }

    private void load() {
        this.load(Locale.ENGLISH);
        this.load(Locale.GERMAN);
    }

    public void loadAndRegister() {
        // load translation files from classpath
        this.load();
        // add to global translation registry
        translator().addSource(this.store);
    }
}
