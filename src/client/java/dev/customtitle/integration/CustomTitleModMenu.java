package dev.customtitle.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.customtitle.screen.TitleLayoutEditorScreen;

public final class CustomTitleModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TitleLayoutEditorScreen::new;
    }
}
