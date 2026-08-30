package dev.customtitle.config;

public final class ElementConfig {
    public Anchor anchor = Anchor.CENTER;
    public double offsetX;
    public double offsetY;
    public int width = 200;
    public int height = 20;
    public float scale = 1.0f;
    public boolean visible = true;
    public int zIndex;
    public int spacing = 4;

    public ElementConfig copy() {
        ElementConfig copy = new ElementConfig();
        copy.anchor = anchor;
        copy.offsetX = offsetX;
        copy.offsetY = offsetY;
        copy.width = width;
        copy.height = height;
        copy.scale = scale;
        copy.visible = visible;
        copy.zIndex = zIndex;
        copy.spacing = spacing;
        return copy;
    }

    public void validate() {
        if (anchor == null) anchor = Anchor.CENTER;
        if (!Double.isFinite(offsetX)) offsetX = 0;
        if (!Double.isFinite(offsetY)) offsetY = 0;
        offsetX = Math.max(-2, Math.min(2, offsetX));
        offsetY = Math.max(-2, Math.min(2, offsetY));
        width = Math.max(20, Math.min(4096, width));
        height = Math.max(10, Math.min(1024, height));
        scale = Math.max(.25f, Math.min(4f, Float.isFinite(scale) ? scale : 1));
        zIndex = Math.max(-100, Math.min(100, zIndex));
        spacing = Math.max(0, Math.min(64, spacing));
    }
}
