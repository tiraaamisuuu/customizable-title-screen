package dev.customtitle.config;

public final class BackgroundConfig {
    public boolean panorama = true;
    public String asset = "";
    public BackgroundMode mode = BackgroundMode.COVER;
    public float dim = 0.20f;
    public float blur = 0.0f;
    public int tint = 0xFFFFFF;
    public float opacity = 1.0f;

    public BackgroundConfig copy() {
        BackgroundConfig copy = new BackgroundConfig();
        copy.panorama = panorama;
        copy.asset = asset;
        copy.mode = mode;
        copy.dim = dim;
        copy.blur = blur;
        copy.tint = tint;
        copy.opacity = opacity;
        return copy;
    }

    public void validate() {
        if (asset == null) asset = "";
        if (mode == null) mode = BackgroundMode.COVER;
        dim = clamp(dim);
        blur = clamp(blur);
        opacity = clamp(opacity);
        tint &= 0xFFFFFF;
    }

    private static float clamp(float value) {
        return Math.max(0, Math.min(1, Float.isFinite(value) ? value : 0));
    }
}
