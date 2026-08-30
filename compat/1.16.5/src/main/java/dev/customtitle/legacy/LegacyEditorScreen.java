package dev.customtitle.legacy;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.text.LiteralText;

public final class LegacyEditorScreen extends Screen {
    private final Screen parent;
    private boolean dragging;
    private double lastX, lastY;

    public LegacyEditorScreen(Screen parent) {
        super(new LiteralText("Customize Title Screen"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addButton(new ButtonWidget(width / 2 - 100, height - 52, 98, 20, new LiteralText("Reset Layout"), button -> {
            LegacyClient.CONFIG.buttonOffsetX = 0;
            LegacyClient.CONFIG.buttonOffsetY = 0;
            LegacyClient.CONFIG.save();
        }));
        addButton(new ButtonWidget(width / 2 + 2, height - 52, 98, 20, new LiteralText("Done"), button -> onClose()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        DrawableHelper.drawCenteredText(matrices, textRenderer, "Customizable Title Screen", width / 2, 20, 0xFFFFFF);
        DrawableHelper.drawCenteredText(matrices, textRenderer, "Drop PNG/JPEG files into config/customizable-title-screen/assets", width / 2, 40, 0xC0C0C0);
        DrawableHelper.drawCenteredText(matrices, textRenderer, "Drag the preview controls; reset or edit the JSON for advanced layout.", width / 2, 55, 0xC0C0C0);
        int x = width / 2 - 100 + LegacyClient.CONFIG.buttonOffsetX;
        int y = height / 2 + LegacyClient.CONFIG.buttonOffsetY;
        fill(matrices, x, y, x + 200, y + 20, 0x804080A0);
        DrawableHelper.drawCenteredText(matrices, textRenderer, "Main Menu Buttons", width / 2 + LegacyClient.CONFIG.buttonOffsetX, y + 6, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = width / 2 - 100 + LegacyClient.CONFIG.buttonOffsetX;
        int y = height / 2 + LegacyClient.CONFIG.buttonOffsetY;
        if (button == 0 && mouseX >= x && mouseX < x + 200 && mouseY >= y && mouseY < y + 20) {
            dragging = true; lastX = mouseX; lastY = mouseY; return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        LegacyClient.CONFIG.buttonOffsetX += (int) Math.round(mouseX - lastX);
        LegacyClient.CONFIG.buttonOffsetY += (int) Math.round(mouseY - lastY);
        LegacyClient.CONFIG.validate(); lastX = mouseX; lastY = mouseY; return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) { dragging = false; LegacyClient.CONFIG.save(); return true; }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() { client.openScreen(parent); }
}
