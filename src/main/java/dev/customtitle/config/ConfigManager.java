package dev.customtitle.config;

import com.google.gson.JsonParseException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigManager {
    public static final long MAX_CONFIG_BYTES = 1_048_576;

    private final Path directory;
    private final Path configPath;
    private final Path backupPath;
    private final Logger logger;
    private ModConfig config = ModConfig.defaults();

    public ConfigManager(Path configDirectory, Logger logger) {
        this.directory = configDirectory.resolve("customizable-title-screen");
        this.configPath = directory.resolve("config.json");
        this.backupPath = directory.resolve("config.json.bak");
        this.logger = logger;
    }

    public synchronized ModConfig load() {
        try {
            Files.createDirectories(assetsDirectory());
            config = read(configPath);
            return config;
        } catch (Exception primaryError) {
            logger.warn("Could not load title-screen configuration; trying backup", primaryError);
            try {
                config = read(backupPath);
                save();
                logger.info("Recovered title-screen configuration from backup");
            } catch (Exception backupError) {
                logger.warn("No usable title-screen backup; restoring safe defaults", backupError);
                config = ModConfig.defaults();
                try { save(); } catch (IOException saveError) { logger.error("Could not persist default title-screen configuration", saveError); }
            }
            return config;
        }
    }

    private ModConfig read(Path path) throws IOException {
        if (!Files.exists(path)) return ModConfig.defaults();
        long size = Files.size(path);
        if (size <= 0 || size > MAX_CONFIG_BYTES) throw new IOException("Configuration has unsafe size: " + size);
        return ConfigCodec.decode(Files.readString(path, StandardCharsets.UTF_8));
    }

    public synchronized void save() throws IOException {
        Files.createDirectories(directory);
        String json = ConfigCodec.encode(config);
        Path temporary = directory.resolve("config.json.tmp");
        Files.writeString(temporary, json, StandardCharsets.UTF_8);
        if (Files.exists(configPath)) Files.copy(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        atomicMove(temporary, configPath);
    }

    public synchronized void exportActiveProfile(Path destination) throws IOException {
        Path target = ensureJsonExtension(destination);
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(temporary, ConfigCodec.encodeProfile(config.active()), StandardCharsets.UTF_8);
        atomicMove(temporary, target);
    }

    public synchronized String importProfile(Path source) throws IOException, JsonParseException {
        long size = Files.size(source);
        if (size <= 0 || size > MAX_CONFIG_BYTES) throw new IOException("Profile has unsafe size: " + size);
        ProfileConfig profile = ConfigCodec.decodeProfile(Files.readString(source, StandardCharsets.UTF_8));
        String baseName = sanitizeName(profile.name);
        String name = baseName;
        int suffix = 2;
        while (config.profiles.containsKey(name)) name = baseName + " " + suffix++;
        profile.name = name;
        config.profiles.put(name, profile);
        config.activeProfile = name;
        save();
        return name;
    }

    public synchronized void replace(ModConfig replacement) {
        replacement.validate();
        config = replacement;
    }

    public synchronized ModConfig config() { return config; }
    public Path directory() { return directory; }
    public Path assetsDirectory() { return directory.resolve("assets"); }

    private static Path ensureJsonExtension(Path path) {
        return path.getFileName().toString().toLowerCase().endsWith(".json") ? path : path.resolveSibling(path.getFileName() + ".json");
    }

    private static String sanitizeName(String input) {
        String name = input == null ? "Imported" : input.replaceAll("[\\p{Cntrl}<>:\"/\\\\|?*]", "_").strip();
        if (name.isBlank()) name = "Imported";
        return name.substring(0, Math.min(40, name.length()));
    }

    private static void atomicMove(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
