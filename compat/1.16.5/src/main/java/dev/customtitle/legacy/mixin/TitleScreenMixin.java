package dev.customtitle.legacy.mixin;

import net.minecraft.client.util.math.MatrixStack;
import dev.customtitle.legacy.LegacyBackground;
import dev.customtitle.legacy.LegacyClient;
import dev.customtitle.legacy.LegacyEditorScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin() { super(new LiteralText("")); }

    @Inject(method = "init", at = @At("TAIL"))
    private void customtitle$addEditor(CallbackInfo info) {
        addButton(new ButtonWidget(width - 154, 6, 148, 20, new LiteralText("Customize Title Screen"), button ->
            client.openScreen(new LegacyEditorScreen((Screen) (Object) this))));
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderBackground(Lnet/minecraft/client/util/math/MatrixStack;)V"), require = 0)
    private void customtitle$background(Screen screen, MatrixStack matrices) {
        if (!LegacyBackground.render(matrices, width, height)) screen.renderBackground(matrices);
    }
}
