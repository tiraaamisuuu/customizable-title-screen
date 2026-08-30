package dev.customtitle.config;

public final class DefaultLayouts {
    private DefaultLayouts() {}

    public static ProfileConfig create() {
        ProfileConfig profile = new ProfileConfig();
        put(profile, "menu.singleplayer", 0, -0.02, 200, 20);
        put(profile, "menu.multiplayer", 0, 0.08, 200, 20);
        put(profile, "menu.online", 0, 0.18, 200, 20);
        put(profile, "menu.options", -0.16, 0.28, 98, 20);
        put(profile, "menu.quit", 0.16, 0.28, 98, 20);
        put(profile, "narrator.button.language", -0.35, 0.28, 20, 20);
        put(profile, "narrator.button.accessibility", 0.35, 0.28, 20, 20);

        ElementConfig logo = put(profile, "customtitle.logo", 0, -0.32, 275, 44);
        logo.scale = 1;
        ElementConfig splash = put(profile, "customtitle.splash", .18, -0.20, 160, 12);
        splash.scale = 1;
        put(profile, "customtitle.edition", 0, -0.18, 120, 10);
        put(profile, "customtitle.version", -0.49, .47, 180, 10).anchor = Anchor.CENTER_LEFT;
        put(profile, "customtitle.copyright", .49, .47, 180, 10).anchor = Anchor.CENTER_RIGHT;
        return profile;
    }

    private static ElementConfig put(ProfileConfig profile, String id, double x, double y, int width, int height) {
        ElementConfig element = new ElementConfig();
        element.offsetX = x;
        element.offsetY = y;
        element.width = width;
        element.height = height;
        profile.elements.put(id, element);
        return element;
    }
}
