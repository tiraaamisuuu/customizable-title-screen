package dev.customtitle.mixin.client;

import dev.customtitle.CustomTitleClient;
import dev.customtitle.config.ElementConfig;
import dev.customtitle.config.ProfileConfig;
import dev.customtitle.layout.LayoutMath;
import dev.customtitle.layout.ResolvedRect;
import dev.customtitle.render.BackgroundRenderer;
import dev.customtitle.screen.TitleLayoutEditorScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void customtitle$addEditorAndApplyLayout(CallbackInfo ci) {
        ProfileConfig profile = CustomTitleClient.config().config().active();
        for (GuiEventListener listener : children()) {
            if (!(listener instanceof AbstractWidget widget)) continue;
            String id = customtitle$id(widget);
            ElementConfig element = profile.elements.get(id);
            if (element == null) continue;
            ResolvedRect rect = LayoutMath.resolve(element, width, height);
            widget.setRectangle(rect.width(), rect.height(), rect.x(), rect.y());
            widget.visible = element.visible;
        }
        addRenderableWidget(Button.builder(Component.translatable("customtitle.customize"), button ->
            minecraft.gui.setScreen(new TitleLayoutEditorScreen((Screen) (Object) this)))
            .bounds(Math.max(4, width - 154), 6, 148, 20).build());
    }

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;extractPanorama(Lnet/minecraft/client/gui/GuiGraphicsExtractor;F)V"))
    private void customtitle$renderBackground(TitleScreen instance, GuiGraphicsExtractor graphics, float partialTick) {
        if (!BackgroundRenderer.extract(graphics, width, height, CustomTitleClient.config().config().active().background)) {
            extractPanorama(graphics, partialTick);
        }
    }

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/LogoRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IF)V"))
    private void customtitle$renderLogo(LogoRenderer logoRenderer, GuiGraphicsExtractor graphics, int screenWidth, float alpha) {
        ProfileConfig profile = CustomTitleClient.config().config().active();
        ElementConfig logo = profile.elements.get("customtitle.logo");
        if (logo == null || logo.visible) {
            ResolvedRect rect = logo == null ? new ResolvedRect(screenWidth / 2 - 128, 30, 256, 44) : LayoutMath.resolve(logo, width, height);
            graphics.blit(RenderPipelines.GUI_TEXTURED, LogoRenderer.MINECRAFT_LOGO, rect.x(), rect.y(), 0, 0, rect.width(), rect.height(), 256, 44, 256, 64, ARGB.white(alpha));
        }
        ElementConfig edition = profile.elements.get("customtitle.edition");
        if (edition == null || edition.visible) {
            ResolvedRect rect = edition == null ? new ResolvedRect(screenWidth / 2 - 64, 67, 128, 14) : LayoutMath.resolve(edition, width, height);
            graphics.blit(RenderPipelines.GUI_TEXTURED, LogoRenderer.MINECRAFT_EDITION, rect.x(), rect.y(), 0, 0, rect.width(), rect.height(), 128, 14, 128, 16, ARGB.white(alpha));
        }
    }

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/SplashRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;ILnet/minecraft/client/gui/Font;F)V"))
    private void customtitle$renderSplash(SplashRenderer splash, GuiGraphicsExtractor graphics, int screenWidth, Font font, float alpha) {
        ElementConfig element = CustomTitleClient.config().config().active().elements.get("customtitle.splash");
        if (element != null && !element.visible) return;
        if (element == null) { splash.extractRenderState(graphics, screenWidth, font, alpha); return; }
        ResolvedRect rect = LayoutMath.resolve(element, width, height);
        graphics.pose().pushMatrix();
        graphics.pose().translate(rect.x() + rect.width() / 2f - (screenWidth / 2f + 123), rect.y() + rect.height() / 2f - 69);
        splash.extractRenderState(graphics, screenWidth, font, alpha);
        graphics.pose().popMatrix();
    }

    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"), require = 0)
    private void customtitle$renderVersion(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color) {
        ElementConfig element = CustomTitleClient.config().config().active().elements.get("customtitle.version");
        if (element == null) { graphics.text(font, text, x, y, color); return; }
        if (!element.visible) return;
        ResolvedRect rect = LayoutMath.resolve(element, width, height);
        graphics.text(font, text, rect.x(), rect.y(), color);
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void customtitle$releaseBackground(CallbackInfo ci) { BackgroundRenderer.release(); }

    private static String customtitle$id(AbstractWidget widget) {
        if (widget.getMessage().getContents() instanceof TranslatableContents translatable) {
            return switch (translatable.getKey()) {
                case "title.credits" -> "customtitle.copyright";
                default -> translatable.getKey();
            };
        }
        return "compatible." + Integer.toHexString(widget.getMessage().getString().hashCode());
    }
}
