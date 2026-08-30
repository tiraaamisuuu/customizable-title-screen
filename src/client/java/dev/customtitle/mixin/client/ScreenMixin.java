package dev.customtitle.mixin.client;

import dev.customtitle.CustomTitleClient;
import dev.customtitle.config.DefaultLayouts;
import dev.customtitle.render.BackgroundRenderer;
import dev.customtitle.screen.TitleLayoutEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void customtitle$emergencyShortcut(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof TitleScreen) || event.key() != 67 || !event.hasControlDown() || !event.hasAltDown()) return;
        if (event.hasShiftDown()) {
            var config = CustomTitleClient.config().config();
            config.profiles.put(config.activeProfile, DefaultLayouts.create());
            try { CustomTitleClient.config().save(); }
            catch (IOException error) { CustomTitleClient.LOGGER.error("Could not persist emergency title-screen reset", error); }
            BackgroundRenderer.release();
            Minecraft.getInstance().gui.setScreen(new TitleScreen());
        } else {
            Minecraft.getInstance().gui.setScreen(new TitleLayoutEditorScreen(self));
        }
        cir.setReturnValue(true);
    }
}
