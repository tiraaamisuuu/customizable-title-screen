package dev.customtitle.layout;

import dev.customtitle.config.ElementConfig;

public final class LayoutMath {
    private LayoutMath() {}

    public static ResolvedRect resolve(ElementConfig element, int screenWidth, int screenHeight) {
        element.validate();
        int width = Math.round(element.width * element.scale);
        int height = Math.round(element.height * element.scale);
        double anchorX = element.anchor.x * screenWidth + element.offsetX * screenWidth;
        double anchorY = element.anchor.y * screenHeight + element.offsetY * screenHeight;
        int x = (int) Math.round(anchorX - element.anchor.x * width);
        int y = (int) Math.round(anchorY - element.anchor.y * height);
        return keepReachable(new ResolvedRect(x, y, width, height), screenWidth, screenHeight);
    }

    public static ResolvedRect keepReachable(ResolvedRect rect, int screenWidth, int screenHeight) {
        int visible = 12;
        int x = Math.max(-rect.width() + visible, Math.min(screenWidth - visible, rect.x()));
        int y = Math.max(0, Math.min(screenHeight - visible, rect.y()));
        return new ResolvedRect(x, y, rect.width(), rect.height());
    }

    public static void moveTo(ElementConfig element, int desiredX, int desiredY, int screenWidth, int screenHeight) {
        int width = Math.round(element.width * element.scale);
        int height = Math.round(element.height * element.scale);
        element.offsetX = (desiredX + element.anchor.x * width - element.anchor.x * screenWidth) / screenWidth;
        element.offsetY = (desiredY + element.anchor.y * height - element.anchor.y * screenHeight) / screenHeight;
        element.validate();
    }

    public static int snap(int value, int size, int screenSize, int threshold) {
        int[] guides = {0, screenSize / 2 - size / 2, screenSize - size};
        for (int guide : guides) if (Math.abs(value - guide) <= threshold) return guide;
        return value;
    }
}
