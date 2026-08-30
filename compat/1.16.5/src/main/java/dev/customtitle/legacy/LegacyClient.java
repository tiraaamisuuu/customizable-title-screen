package dev.customtitle.legacy;

import net.fabricmc.api.ClientModInitializer;

public final class LegacyClient implements ClientModInitializer {
    public static final String MOD_ID = "customizable-title-screen-legacy";
    public static final LegacyConfig CONFIG = LegacyConfig.load();

    @Override
    public void onInitializeClient() {}
}
