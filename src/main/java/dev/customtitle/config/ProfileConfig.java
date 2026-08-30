package dev.customtitle.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProfileConfig {
    public String name = "Default";
    public BackgroundConfig background = new BackgroundConfig();
    public Map<String, ElementConfig> elements = new LinkedHashMap<>();

    public ProfileConfig copy() {
        ProfileConfig copy = new ProfileConfig();
        copy.name = name;
        copy.background = background.copy();
        elements.forEach((key, value) -> copy.elements.put(key, value.copy()));
        return copy;
    }

    public void validate(String fallbackName) {
        if (name == null || name.isBlank()) name = fallbackName;
        name = name.strip().substring(0, Math.min(48, name.strip().length()));
        if (background == null) background = new BackgroundConfig();
        background.validate();
        if (elements == null) elements = new LinkedHashMap<>();
        elements.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null);
        elements.values().forEach(ElementConfig::validate);
    }
}
