package dev.customtitle.layout;

import dev.customtitle.config.Anchor;
import dev.customtitle.config.ElementConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LayoutMathTest {
    @Test
    void centerAnchorRemainsCenteredAcrossAspectRatios() {
        ElementConfig element = new ElementConfig();
        element.anchor = Anchor.CENTER;
        element.width = 200;
        element.height = 20;
        ResolvedRect normal = LayoutMath.resolve(element, 320, 240);
        ResolvedRect ultraWide = LayoutMath.resolve(element, 640, 240);
        assertEquals(60, normal.x());
        assertEquals(220, ultraWide.x());
        assertEquals(110, normal.y());
        assertEquals(110, ultraWide.y());
    }

    @Test
    void scalingChangesBoundsAroundAnchor() {
        ElementConfig element = new ElementConfig();
        element.scale = 1.5f;
        ResolvedRect rect = LayoutMath.resolve(element, 400, 300);
        assertEquals(300, rect.width());
        assertEquals(30, rect.height());
        assertEquals(50, rect.x());
        assertEquals(135, rect.y());
    }

    @Test
    void moveAndResolveAreInverseWithinRounding() {
        ElementConfig element = new ElementConfig();
        element.anchor = Anchor.BOTTOM_RIGHT;
        LayoutMath.moveTo(element, 27, 42, 500, 300);
        ResolvedRect rect = LayoutMath.resolve(element, 500, 300);
        assertEquals(27, rect.x());
        assertEquals(42, rect.y());
    }

    @Test
    void snappingFindsEdgesAndCenter() {
        assertEquals(0, LayoutMath.snap(4, 20, 320, 5));
        assertEquals(150, LayoutMath.snap(153, 20, 320, 5));
        assertEquals(300, LayoutMath.snap(297, 20, 320, 5));
        assertEquals(88, LayoutMath.snap(88, 20, 320, 5));
    }
}
