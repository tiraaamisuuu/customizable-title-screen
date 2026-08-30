package dev.customtitle;

import dev.customtitle.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CustomTitleClient implements ClientModInitializer {
    public static final String MOD_ID = "customizable-title-screen";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ConfigManager configManager;

    @Override
    public void onInitializeClient() {
        configManager = new ConfigManager(FabricLoader.getInstance().getConfigDir(), LOGGER);
        configManager.load();
        LOGGER.info("Customizable Title Screen initialized with profile '{}'", configManager.config().activeProfile);
    }

    public static ConfigManager config() {
        if (configManager == null) throw new IllegalStateException("Customizable Title Screen has not initialized");
        return configManager;
    }
}
