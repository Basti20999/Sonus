package dev.minceraft.sonus.web.pion.launcher;

import org.jspecify.annotations.NullMarked;

import java.util.Locale;

@NullMarked
public enum SystemArch {

    AMD64("amd64", "amd64", "x86_64"),
    ARM64("arm64", "arm64", "aarch64"),
    UNSUPPORTED("unsupported"),
    ;

    private final String id;
    private final String[] ids;

    SystemArch(String id, String... ids) {
        this.id = id;
        this.ids = ids;
    }

    public static SystemArch detect() {
        String name = System.getProperty("os.arch", "unknown").toLowerCase(Locale.ROOT);
        for (SystemArch os : SystemArch.values()) {
            if (os.is(name)) {
                return os;
            }
        }
        return UNSUPPORTED;
    }

    public boolean is(String string) {
        for (String id : this.ids) {
            if (string.contains(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
