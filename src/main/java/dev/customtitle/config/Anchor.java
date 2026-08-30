package dev.customtitle.config;

public enum Anchor {
    TOP_LEFT(0, 0), TOP_CENTER(.5, 0), TOP_RIGHT(1, 0),
    CENTER_LEFT(0, .5), CENTER(.5, .5), CENTER_RIGHT(1, .5),
    BOTTOM_LEFT(0, 1), BOTTOM_CENTER(.5, 1), BOTTOM_RIGHT(1, 1);

    public final double x;
    public final double y;

    Anchor(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
