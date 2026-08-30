package dev.customtitle.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public final class ConfigCodec {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private ConfigCodec() {}

    public static ModConfig decode(String json) {
        if (json == null || json.isBlank()) throw new JsonParseException("Configuration is empty");
        JsonElement rootElement = GSON.fromJson(json, JsonElement.class);
        if (rootElement == null || !rootElement.isJsonObject()) throw new JsonParseException("Configuration root must be an object");
        JsonObject root = rootElement.getAsJsonObject();
        int schema = root.has("schemaVersion") ? root.get("schemaVersion").getAsInt() : 1;
        if (schema > ModConfig.CURRENT_SCHEMA) throw new JsonParseException("Configuration schema " + schema + " is newer than supported schema " + ModConfig.CURRENT_SCHEMA);
        if (schema < 1) throw new JsonParseException("Invalid configuration schema");
        if (schema == 1) migrateV1(root);
        ModConfig config = GSON.fromJson(root, ModConfig.class);
        if (config == null) throw new JsonParseException("Configuration could not be decoded");
        config.validate();
        return config;
    }

    private static void migrateV1(JsonObject root) {
        if (!root.has("profiles")) {
            JsonObject profiles = new JsonObject();
            JsonElement oldProfile = root.remove("profile");
            profiles.add("Default", oldProfile != null && oldProfile.isJsonObject() ? oldProfile : new JsonObject());
            root.add("profiles", profiles);
            root.addProperty("activeProfile", "Default");
        }
        root.addProperty("schemaVersion", ModConfig.CURRENT_SCHEMA);
    }

    public static String encode(ModConfig config) {
        config.validate();
        return GSON.toJson(config);
    }

    public static ProfileConfig decodeProfile(String json) {
        JsonElement element = GSON.fromJson(json, JsonElement.class);
        if (element == null || !element.isJsonObject()) throw new JsonParseException("Profile root must be an object");
        JsonObject object = element.getAsJsonObject();
        JsonElement payload = object.has("profile") ? object.get("profile") : object;
        ProfileConfig profile = GSON.fromJson(payload, ProfileConfig.class);
        if (profile == null) throw new JsonParseException("Profile could not be decoded");
        profile.validate("Imported");
        return profile;
    }

    public static String encodeProfile(ProfileConfig profile) {
        JsonObject export = new JsonObject();
        export.addProperty("format", "customizable-title-screen-profile");
        export.addProperty("schemaVersion", ModConfig.CURRENT_SCHEMA);
        export.add("profile", GSON.toJsonTree(profile));
        return GSON.toJson(export);
    }
}
