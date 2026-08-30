package dev.customtitle.config;

import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigCodecTest {
    @Test
    void roundTripsReadableCurrentSchema() {
        ModConfig original = ModConfig.defaults();
        original.active().background.panorama = false;
        original.active().background.asset = "mountains.png";
        String json = ConfigCodec.encode(original);
        ModConfig decoded = ConfigCodec.decode(json);
        assertEquals(ModConfig.CURRENT_SCHEMA, decoded.schemaVersion);
        assertEquals("mountains.png", decoded.active().background.asset);
        assertTrue(json.contains("\"profiles\""));
    }

    @Test
    void migratesSingleProfileSchemaOne() {
        String json = """
            {"schemaVersion":1,"profile":{"name":"Old","background":{"panorama":true},"elements":{}}}
            """;
        ModConfig decoded = ConfigCodec.decode(json);
        assertEquals(2, decoded.schemaVersion);
        assertEquals("Default", decoded.activeProfile);
        assertEquals("Old", decoded.active().name);
    }

    @Test
    void rejectsFutureAndNonObjectConfigurations() {
        assertThrows(JsonParseException.class, () -> ConfigCodec.decode("[]"));
        assertThrows(JsonParseException.class, () -> ConfigCodec.decode("{\"schemaVersion\":999}"));
        assertThrows(JsonParseException.class, () -> ConfigCodec.decode(""));
    }

    @Test
    void clampsInvalidNumbersAndRestoresRequiredValues() {
        String json = """
            {"schemaVersion":2,"activeProfile":"Missing","profiles":{"A":{"name":"A","background":{"dim":8,"blur":-2,"opacity":9},"elements":{"x":{"width":-5,"height":99999,"scale":50,"offsetX":99}}}}}
            """;
        ModConfig decoded = ConfigCodec.decode(json);
        assertEquals("A", decoded.activeProfile);
        ElementConfig element = decoded.active().elements.get("x");
        assertEquals(20, element.width);
        assertEquals(1024, element.height);
        assertEquals(4, element.scale);
        assertEquals(2, element.offsetX);
        assertEquals(1, decoded.active().background.dim);
        assertEquals(0, decoded.active().background.blur);
    }
}
