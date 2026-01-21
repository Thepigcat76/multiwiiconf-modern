package config;

import java.util.Map;

public record Config(Map<String, Entry> entries, String version) {
    public static final Config EMPTY = new Config(Map.of(), "1.0");

    public Entry getEntry(String key) {
        return this.entries().get(key);
    }

    public record Entry(String key, float value) {
    }
}
