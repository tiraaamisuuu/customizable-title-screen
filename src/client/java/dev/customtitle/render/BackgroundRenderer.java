package dev.customtitle.render;

import com.mojang.blaze3d.platform.NativeImage;
import dev.customtitle.CustomTitleClient;
import dev.customtitle.asset.AssetImporter;
import dev.customtitle.asset.ImageValidator;
import dev.customtitle.config.BackgroundConfig;
import dev.customtitle.config.BackgroundMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BackgroundRenderer {
    private static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath(CustomTitleClient.MOD_ID, "runtime/title_background");
    private static String loadedAsset = "";
    private static float loadedBlur = -1;
    private static int textureWidth;
    private static int textureHeight;
    private static String lastFailure = "";

    private BackgroundRenderer() {}

    public static boolean extract(GuiGraphicsExtractor graphics, int width, int height, BackgroundConfig config) {
        if (config.panorama || config.asset.isBlank()) {
            release();
            return false;
        }
        if (!ensureLoaded(config)) return false;

        int alpha = Math.max(0, Math.min(255, Math.round(config.opacity * 255)));
        int color = (alpha << 24) | config.tint;
        graphics.fill(0, 0, width, height, 0xFF000000);
        drawImage(graphics, width, height, config.mode, color);
        if (config.dim > 0) {
            int dimAlpha = Math.round(config.dim * 220);
            graphics.fill(0, 0, width, height, dimAlpha << 24);
        }
        return true;
    }

    private static boolean ensureLoaded(BackgroundConfig config) {
        if (config.asset.equals(loadedAsset) && Float.compare(config.blur, loadedBlur) == 0 && textureWidth > 0) return true;
        release();
        try {
            Path path = AssetImporter.resolveAsset(CustomTitleClient.config().assetsDirectory(), config.asset);
            ImageValidator.inspect(path);
            NativeImage image = load(path, config.blur);
            textureWidth = image.getWidth();
            textureHeight = image.getHeight();
            Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, new DynamicTexture(() -> "Custom title background", image));
            loadedAsset = config.asset;
            loadedBlur = config.blur;
            lastFailure = "";
            return true;
        } catch (Exception error) {
            String failure = config.asset + ":" + error.getMessage();
            if (!failure.equals(lastFailure)) {
                CustomTitleClient.LOGGER.warn("Could not load custom title background '{}'; using panorama", config.asset, error);
                lastFailure = failure;
            }
            return false;
        }
    }

    private static NativeImage load(Path path, float blur) throws IOException {
        if (blur <= .001f) {
            try (var stream = Files.newInputStream(path)) { return NativeImage.read(stream); }
        }
        BufferedImage source = ImageIO.read(path.toFile());
        if (source == null) throw new IOException("Unsupported or corrupt image");
        double scale = Math.min(1, 2048.0 / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage working = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = working.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        int radius = Math.max(1, Math.min(6, Math.round(blur * 6)));
        int side = radius * 2 + 1;
        float[] kernel = new float[side * side];
        java.util.Arrays.fill(kernel, 1f / kernel.length);
        BufferedImage blurred = new ConvolveOp(new Kernel(side, side, kernel), ConvolveOp.EDGE_NO_OP, null).filter(working, null);
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        ImageIO.write(blurred, "png", encoded);
        return NativeImage.read(new ByteArrayInputStream(encoded.toByteArray()));
    }

    private static void drawImage(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, BackgroundMode mode, int color) {
        switch (mode) {
            case STRETCH -> blit(graphics, 0, 0, screenWidth, screenHeight, 0, 0, textureWidth, textureHeight, color);
            case CONTAIN -> {
                double scale = Math.min((double) screenWidth / textureWidth, (double) screenHeight / textureHeight);
                int width = Math.max(1, (int) Math.round(textureWidth * scale));
                int height = Math.max(1, (int) Math.round(textureHeight * scale));
                blit(graphics, (screenWidth - width) / 2, (screenHeight - height) / 2, width, height, 0, 0, textureWidth, textureHeight, color);
            }
            case CENTER -> {
                int drawWidth = Math.min(screenWidth, textureWidth);
                int drawHeight = Math.min(screenHeight, textureHeight);
                int sourceX = Math.max(0, (textureWidth - drawWidth) / 2);
                int sourceY = Math.max(0, (textureHeight - drawHeight) / 2);
                blit(graphics, (screenWidth - drawWidth) / 2, (screenHeight - drawHeight) / 2, drawWidth, drawHeight, sourceX, sourceY, drawWidth, drawHeight, color);
            }
            case TILE -> {
                int tileWidth = Math.max(1, Math.min(textureWidth, 1024));
                int tileHeight = Math.max(1, Math.min(textureHeight, 1024));
                for (int y = 0; y < screenHeight; y += tileHeight) {
                    for (int x = 0; x < screenWidth; x += tileWidth) {
                        int width = Math.min(tileWidth, screenWidth - x);
                        int height = Math.min(tileHeight, screenHeight - y);
                        blit(graphics, x, y, width, height, 0, 0, width, height, color);
                    }
                }
            }
            case COVER -> {
                double screenAspect = (double) screenWidth / screenHeight;
                double imageAspect = (double) textureWidth / textureHeight;
                int sourceX = 0, sourceY = 0, sourceWidth = textureWidth, sourceHeight = textureHeight;
                if (imageAspect > screenAspect) {
                    sourceWidth = Math.max(1, (int) Math.round(textureHeight * screenAspect));
                    sourceX = (textureWidth - sourceWidth) / 2;
                } else {
                    sourceHeight = Math.max(1, (int) Math.round(textureWidth / screenAspect));
                    sourceY = (textureHeight - sourceHeight) / 2;
                }
                blit(graphics, 0, 0, screenWidth, screenHeight, sourceX, sourceY, sourceWidth, sourceHeight, color);
            }
        }
    }

    private static void blit(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int u, int v, int sourceWidth, int sourceHeight, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, x, y, u, v, width, height, sourceWidth, sourceHeight, textureWidth, textureHeight, color);
    }

    public static void release() {
        if (textureWidth <= 0) return;
        Minecraft.getInstance().getTextureManager().release(TEXTURE_ID);
        textureWidth = 0;
        textureHeight = 0;
        loadedAsset = "";
        loadedBlur = -1;
    }
}
