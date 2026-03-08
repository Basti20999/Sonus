package dev.minceraft.sonus.web.pion.launcher;

import org.jspecify.annotations.NullMarked;

import java.util.Locale;

@NullMarked
public enum OperatingSystem {

    DARWIN("darwin", "darwin", "mac"),
    LINUX("linux", "nux"),
    WINDOWS("windows", "win"),
    UNSUPPORTED("unsupported"),
    ;

    private final String id;
    private final String[] ids;

    OperatingSystem(String id, String... ids) {
        this.id = id;
        this.ids = ids;
    }

    public static OperatingSystem detect() {
        String name = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
        for (OperatingSystem os : OperatingSystem.values()) {
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
