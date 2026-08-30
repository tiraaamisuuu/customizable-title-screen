package dev.customtitle.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModConfig {
    public static final int CURRENT_SCHEMA = 2;

    public int schemaVersion = CURRENT_SCHEMA;
    public String activeProfile = "Default";
    public Map<String, ProfileConfig> profiles = new LinkedHashMap<>();

    public static ModConfig defaults() {
        ModConfig config = new ModConfig();
        config.profiles.put("Default", DefaultLayouts.create());
        return config;
    }

    public ProfileConfig active() {
        return profiles.computeIfAbsent(activeProfile, key -> {
            ProfileConfig profile = DefaultLayouts.create();
            profile.name = key;
            return profile;
        });
    }

    public void validate() {
        schemaVersion = CURRENT_SCHEMA;
        if (profiles == null) profiles = new LinkedHashMap<>();
        profiles.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null);
        if (profiles.isEmpty()) profiles.put("Default", DefaultLayouts.create());
        profiles.forEach((name, profile) -> profile.validate(name));
        if (activeProfile == null || !profiles.containsKey(activeProfile)) activeProfile = profiles.keySet().iterator().next();
    }
}
