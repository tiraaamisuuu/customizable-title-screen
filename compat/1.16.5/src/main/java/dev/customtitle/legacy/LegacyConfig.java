package dev.customtitle.legacy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LegacyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public String background = "";
    public boolean panorama = true;
    public int buttonOffsetX = 0;
    public int buttonOffsetY = 0;
    public float opacity = 1.0f;

    public static LegacyConfig load() {
        Path path = path();
        try {
            if (Files.exists(path)) {
                LegacyConfig config = GSON.fromJson(new String(Files.readAllBytes(path), StandardCharsets.UTF_8), LegacyConfig.class);
                if (config != null) { config.validate(); return config; }
            }
        } catch (Exception ignored) {}
        LegacyConfig config = new LegacyConfig();
        config.save();
        return config;
    }

    public void save() {
        validate();
        try {
            Files.createDirectories(path().getParent());
            Path temp = path().resolveSibling(path().getFileName() + ".tmp");
            Files.write(temp, GSON.toJson(this).getBytes(StandardCharsets.UTF_8));
            Files.move(temp, path(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {}
    }

    public void validate() {
        if (background == null) background = "";
        buttonOffsetX = Math.max(-500, Math.min(500, buttonOffsetX));
        buttonOffsetY = Math.max(-500, Math.min(500, buttonOffsetY));
        if (!Float.isFinite(opacity)) opacity = 1;
        opacity = Math.max(0, Math.min(1, opacity));
    }

    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("customizable-title-screen-legacy.json"); }
}
