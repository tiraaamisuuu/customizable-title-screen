package dev.customtitle.legacy;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.util.Identifier;
import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LegacyBackground {
    private static final Identifier ID = new Identifier(LegacyClient.MOD_ID, "background");
    private static boolean attempted;
    private static int width;
    private static int height;

    private LegacyBackground() {}

    public static boolean render(MatrixStack matrices, int screenWidth, int screenHeight) {
        if (!ensureLoaded()) return false;
        RenderSystem.enableTexture();
        MinecraftClient.getInstance().getTextureManager().bindTexture(ID);
        DrawableHelper.drawTexture(matrices, 0, 0, 0, 0, screenWidth, screenHeight, width, height);
        return true;
    }

    private static boolean ensureLoaded() {
        if (attempted) return width > 0;
        attempted = true;
        if (LegacyClient.CONFIG.panorama || LegacyClient.CONFIG.background.isEmpty()) return false;
        try {
            Path base = FabricLoader.getInstance().getConfigDir().resolve("customizable-title-screen").resolve("assets");
            Path path = base.resolve(LegacyClient.CONFIG.background).normalize();
            if (!path.startsWith(base) || !Files.isRegularFile(path)) return false;
            try (InputStream input = Files.newInputStream(path)) {
                NativeImage image = NativeImage.read(input);
                width = image.getWidth();
                height = image.getHeight();
                MinecraftClient.getInstance().getTextureManager().registerTexture(ID, new NativeImageBackedTexture(image));
            }
            return true;
        } catch (Exception error) {
            return false;
        }
    }
}
