package dev.customtitle.screen;

import dev.customtitle.CustomTitleClient;
import dev.customtitle.asset.AssetImporter;
import dev.customtitle.config.Anchor;
import dev.customtitle.config.BackgroundMode;
import dev.customtitle.config.DefaultLayouts;
import dev.customtitle.config.ElementConfig;
import dev.customtitle.config.ModConfig;
import dev.customtitle.config.ProfileConfig;
import dev.customtitle.editor.EditorHistory;
import dev.customtitle.layout.LayoutMath;
import dev.customtitle.layout.ResolvedRect;
import dev.customtitle.render.BackgroundRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TitleLayoutEditorScreen extends Screen {
    private final Screen parent;
    private final ProfileConfig original;
    private ProfileConfig working;
    private final EditorHistory<ProfileConfig> history = new EditorHistory<>(64, ProfileConfig::copy);
    private String selectedId;
    private int page;
    private int preset;
    private boolean draggingElement;
    private double dragOffsetX;
    private double dragOffsetY;
    private String status = "";

    public TitleLayoutEditorScreen(Screen parent) {
        super(Component.translatable("customtitle.editor.title"));
        this.parent = parent;
        this.original = CustomTitleClient.config().config().active().copy();
        this.working = original.copy();
        this.selectedId = working.elements.keySet().stream().findFirst().orElse(null);
    }

    @Override
    protected void init() {
        int panelX = Math.max(180, width - 176);
        addRenderableWidget(Button.builder(Component.literal(pageName()), button -> { page = (page + 1) % 3; rebuildWidgets(); })
            .bounds(panelX + 4, 24, 168, 20).build());
        if (page == 0) initLayoutPage(panelX);
        else if (page == 1) initBackgroundPage(panelX);
        else initProfilePage(panelX);

        int bottom = height - 24;
        addRenderableWidget(Button.builder(Component.translatable("customtitle.editor.save"), button -> saveAndClose())
            .bounds(4, bottom, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("customtitle.editor.apply"), button -> applyAndClose())
            .bounds(78, bottom, 70, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> cancelAndClose())
            .bounds(152, bottom, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Undo"), button -> undo())
            .bounds(226, bottom, 54, 20).build()).active = history.canUndo();
        addRenderableWidget(Button.builder(Component.literal("Redo"), button -> redo())
            .bounds(284, bottom, 54, 20).build()).active = history.canRedo();
    }

    private void initLayoutPage(int x) {
        int y = 48;
        addPair(x, y, "Previous", button -> select(-1), "Next", button -> select(1)); y += 24;
        addPair(x, y, "Visible", button -> mutate(() -> selected().visible = !selected().visible), "Anchor", button -> mutate(() -> selected().anchor = next(selected().anchor))); y += 24;
        addPair(x, y, "Width -", button -> mutate(() -> selected().width -= 10), "Width +", button -> mutate(() -> selected().width += 10)); y += 24;
        addPair(x, y, "Height -", button -> mutate(() -> selected().height -= 2), "Height +", button -> mutate(() -> selected().height += 2)); y += 24;
        addPair(x, y, "Scale -", button -> mutate(() -> selected().scale -= .1f), "Scale +", button -> mutate(() -> selected().scale += .1f)); y += 24;
        addPair(x, y, "Reset Element", button -> resetElement(), "Preview", button -> { preset = (preset + 1) % 4; }); y += 24;
        addPair(x, y, "Reset All", button -> confirmReset(), "Open Folder", button -> openFolder());
    }

    private void initBackgroundPage(int x) {
        int y = 48;
        addWide(x, y, working.background.panorama ? "Background: Panorama" : "Background: " + working.background.asset, button -> cycleAsset()); y += 24;
        addPair(x, y, "Import Folder", button -> openAssets(), "Panorama", button -> mutate(() -> working.background.panorama = true)); y += 24;
        addWide(x, y, "Mode: " + working.background.mode, button -> mutate(() -> working.background.mode = next(working.background.mode))); y += 24;
        addPair(x, y, "Dim -", button -> mutate(() -> working.background.dim -= .1f), "Dim +", button -> mutate(() -> working.background.dim += .1f)); y += 24;
        addPair(x, y, "Blur -", button -> mutate(() -> working.background.blur -= .1f), "Blur +", button -> mutate(() -> working.background.blur += .1f)); y += 24;
        addPair(x, y, "Opacity -", button -> mutate(() -> working.background.opacity -= .1f), "Opacity +", button -> mutate(() -> working.background.opacity += .1f)); y += 24;
        addWide(x, y, "Tint: " + String.format("#%06X", working.background.tint), button -> mutate(() -> working.background.tint = nextTint(working.background.tint)));
    }

    private void initProfilePage(int x) {
        int y = 48;
        addWide(x, y, "Profile: " + CustomTitleClient.config().config().activeProfile, button -> cycleProfile()); y += 24;
        addWide(x, y, "Duplicate Profile", button -> duplicateProfile()); y += 24;
        addWide(x, y, "Export active-profile.json", button -> exportProfile()); y += 24;
        addWide(x, y, "Import import-profile.json", button -> importProfile()); y += 24;
        addWide(x, y, "Open Config Folder", button -> openFolder());
    }

    private void addPair(int x, int y, String left, Button.OnPress leftAction, String right, Button.OnPress rightAction) {
        addRenderableWidget(Button.builder(Component.literal(left), leftAction).bounds(x + 4, y, 82, 20).build());
        addRenderableWidget(Button.builder(Component.literal(right), rightAction).bounds(x + 90, y, 82, 20).build());
    }

    private void addWide(int x, int y, String text, Button.OnPress action) {
        addRenderableWidget(Button.builder(Component.literal(text), action).bounds(x + 4, y, 168, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (!BackgroundRenderer.extract(graphics, width, height, working.background)) extractPanorama(graphics, partialTick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int panelX = Math.max(180, width - 176);
        graphics.fill(panelX, 0, width, height, 0xE0101010);
        graphics.fill(0, 0, panelX, 22, 0xB0000000);
        graphics.centeredText(font, title, panelX / 2, 7, 0xFFFFFFFF);
        Preview preview = preview(panelX);
        graphics.outline(preview.x, preview.y, preview.width, preview.height, 0xFF80E8FF);
        graphics.verticalLine(preview.x + preview.width / 2, preview.y, preview.y + preview.height, 0x8080E8FF);
        graphics.horizontalLine(preview.x, preview.x + preview.width, preview.y + preview.height / 2, 0x8080E8FF);
        for (var entry : working.elements.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue().zIndex)).toList()) {
            ResolvedRect rect = resolvePreview(entry.getValue(), preview);
            int color = entry.getKey().equals(selectedId) ? 0xFFFFFF40 : entry.getValue().visible ? 0xFF40E080 : 0xFF888888;
            graphics.fill(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + rect.height(), (color & 0x00FFFFFF) | 0x30000000);
            graphics.outline(rect.x(), rect.y(), rect.width(), rect.height(), color);
            graphics.text(font, label(entry.getKey()), rect.x() + 3, rect.y() + Math.max(2, (rect.height() - 8) / 2), color, false);
        }
        graphics.text(font, pageName(), panelX + 8, 8, 0xFFFFFFFF);
        if (selectedId != null) graphics.text(font, "Selected: " + label(selectedId), 6, 26, 0xFFFFFFFF);
        graphics.text(font, presetName(), 6, 38, 0xFF80E8FF);
        if (!status.isBlank()) graphics.text(font, status, 6, height - 36, 0xFFFFCC55);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int panelX = Math.max(180, width - 176);
        if (event.button() == 0 && event.x() < panelX && event.y() < height - 24) {
            Preview preview = preview(panelX);
            List<String> ids = new ArrayList<>(working.elements.keySet());
            for (int i = ids.size() - 1; i >= 0; i--) {
                String id = ids.get(i);
                ResolvedRect rect = resolvePreview(working.elements.get(id), preview);
                if (rect.contains(event.x(), event.y())) {
                    selectedId = id;
                    history.checkpoint(working);
                    draggingElement = true;
                    dragOffsetX = event.x() - rect.x();
                    dragOffsetY = event.y() - rect.y();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggingElement && selectedId != null) {
            Preview preview = preview(Math.max(180, width - 176));
            ElementConfig element = selected();
            int desiredX = (int) Math.round(event.x() - dragOffsetX - preview.x);
            int desiredY = (int) Math.round(event.y() - dragOffsetY - preview.y);
            ResolvedRect scaled = LayoutMath.resolve(element, preview.logicalWidth, preview.logicalHeight);
            if (!event.hasShiftDown()) {
                desiredX = LayoutMath.snap(desiredX, scaled.width(), preview.logicalWidth, 5);
                desiredY = LayoutMath.snap(desiredY, scaled.height(), preview.logicalHeight, 5);
            }
            LayoutMath.moveTo(element, desiredX, desiredY, preview.logicalWidth, preview.logicalHeight);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingElement) {
            draggingElement = false;
            rebuildWidgets();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.hasControlDown() && event.key() == 90) { undo(); return true; }
        if (event.hasControlDown() && event.key() == 89) { redo(); return true; }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() { cancelAndClose(); }

    private void mutate(Runnable mutation) {
        history.checkpoint(working);
        mutation.run();
        working.validate(working.name);
        BackgroundRenderer.release();
        rebuildWidgets();
    }

    private void undo() { working = history.undo(working); BackgroundRenderer.release(); rebuildWidgets(); }
    private void redo() { working = history.redo(working); BackgroundRenderer.release(); rebuildWidgets(); }
    private ElementConfig selected() { return working.elements.get(selectedId); }

    private void select(int direction) {
        List<String> ids = new ArrayList<>(working.elements.keySet());
        if (ids.isEmpty()) return;
        int index = Math.max(0, ids.indexOf(selectedId));
        selectedId = ids.get(Math.floorMod(index + direction, ids.size()));
    }

    private void resetElement() {
        if (selectedId == null) return;
        history.checkpoint(working);
        ElementConfig defaults = DefaultLayouts.create().elements.get(selectedId);
        if (defaults != null) working.elements.put(selectedId, defaults.copy());
        else working.elements.remove(selectedId);
        rebuildWidgets();
    }

    private void confirmReset() {
        minecraft.gui.setScreen(new ConfirmScreen(result -> {
            minecraft.gui.setScreen(this);
            if (result) {
                history.checkpoint(working);
                working = DefaultLayouts.create();
                selectedId = working.elements.keySet().iterator().next();
                BackgroundRenderer.release();
                rebuildWidgets();
            }
        }, Component.translatable("customtitle.editor.confirm_reset"), Component.translatable("customtitle.editor.emergency"), Component.translatable("customtitle.editor.confirm"), Component.translatable("customtitle.editor.keep")));
    }

    private void cycleAsset() {
        try {
            List<Path> images;
            try (var stream = Files.list(CustomTitleClient.config().assetsDirectory())) {
                images = stream.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().toLowerCase().matches(".*\\.(png|jpe?g)$")).sorted().toList();
            }
            if (images.isEmpty()) { status = "Place a PNG/JPEG in the assets folder, then click again."; openAssets(); return; }
            int index = -1;
            for (int i = 0; i < images.size(); i++) if (images.get(i).getFileName().toString().equals(working.background.asset)) index = i;
            Path chosen = images.get((index + 1) % images.size());
            mutate(() -> { working.background.asset = chosen.getFileName().toString(); working.background.panorama = false; });
        } catch (IOException error) { status = error.getMessage(); }
    }

    private void openAssets() { openPath(CustomTitleClient.config().assetsDirectory()); }
    private void openFolder() { openPath(CustomTitleClient.config().directory()); }
    private void openPath(Path path) {
        try { Files.createDirectories(path); Util.getPlatform().openPath(path); }
        catch (IOException error) { status = error.getMessage(); }
    }

    private void exportProfile() {
        try {
            CustomTitleClient.config().config().profiles.put(CustomTitleClient.config().config().activeProfile, working.copy());
            CustomTitleClient.config().exportActiveProfile(CustomTitleClient.config().directory().resolve("active-profile.json"));
            status = "Exported active-profile.json";
        } catch (IOException error) { status = error.getMessage(); }
    }

    private void importProfile() {
        try {
            String name = CustomTitleClient.config().importProfile(CustomTitleClient.config().directory().resolve("import-profile.json"));
            working = CustomTitleClient.config().config().active().copy();
            selectedId = working.elements.keySet().stream().findFirst().orElse(null);
            status = "Imported " + name;
            rebuildWidgets();
        } catch (Exception error) { status = error.getMessage(); }
    }

    private void duplicateProfile() {
        ModConfig config = CustomTitleClient.config().config();
        String base = config.activeProfile + " Copy";
        String name = base;
        for (int i = 2; config.profiles.containsKey(name); i++) name = base + " " + i;
        working.name = name;
        config.profiles.put(name, working.copy());
        config.activeProfile = name;
        status = "Created " + name;
        rebuildWidgets();
    }

    private void cycleProfile() {
        ModConfig config = CustomTitleClient.config().config();
        config.profiles.put(config.activeProfile, working.copy());
        List<String> names = new ArrayList<>(config.profiles.keySet());
        int index = Math.max(0, names.indexOf(config.activeProfile));
        config.activeProfile = names.get((index + 1) % names.size());
        working = config.active().copy();
        selectedId = working.elements.keySet().stream().findFirst().orElse(null);
        BackgroundRenderer.release();
        rebuildWidgets();
    }

    private void applyAndClose() {
        CustomTitleClient.config().config().profiles.put(CustomTitleClient.config().config().activeProfile, working.copy());
        minecraft.gui.setScreen(parent != null ? parent : new TitleScreen());
    }

    private void saveAndClose() {
        try {
            CustomTitleClient.config().config().profiles.put(CustomTitleClient.config().config().activeProfile, working.copy());
            CustomTitleClient.config().save();
            minecraft.gui.setScreen(parent != null ? parent : new TitleScreen());
        } catch (IOException error) { status = error.getMessage(); }
    }

    private void cancelAndClose() {
        CustomTitleClient.config().config().profiles.put(CustomTitleClient.config().config().activeProfile, original.copy());
        BackgroundRenderer.release();
        minecraft.gui.setScreen(parent != null ? parent : new TitleScreen());
    }

    private Preview preview(int panelX) {
        int availableWidth = Math.max(80, panelX - 8);
        int availableHeight = Math.max(60, height - 72);
        int logicalWidth = switch (preset) { case 1 -> 320; case 2 -> 400; case 3 -> 512; default -> availableWidth; };
        int logicalHeight = switch (preset) { case 1 -> 240; case 2 -> 225; case 3 -> 216; default -> availableHeight; };
        double scale = Math.min((double) availableWidth / logicalWidth, (double) availableHeight / logicalHeight);
        int previewWidth = Math.max(1, (int) Math.round(logicalWidth * scale));
        int previewHeight = Math.max(1, (int) Math.round(logicalHeight * scale));
        return new Preview((panelX - previewWidth) / 2, 48 + (availableHeight - previewHeight) / 2, previewWidth, previewHeight, logicalWidth, logicalHeight);
    }

    private ResolvedRect resolvePreview(ElementConfig element, Preview preview) {
        ResolvedRect logical = LayoutMath.resolve(element, preview.logicalWidth, preview.logicalHeight);
        double sx = (double) preview.width / preview.logicalWidth;
        double sy = (double) preview.height / preview.logicalHeight;
        return new ResolvedRect(preview.x + (int) Math.round(logical.x() * sx), preview.y + (int) Math.round(logical.y() * sy), Math.max(2, (int) Math.round(logical.width() * sx)), Math.max(2, (int) Math.round(logical.height() * sy)));
    }

    private String pageName() { return switch (page) { case 1 -> "Background"; case 2 -> "Profiles & Files"; default -> "Layout"; }; }
    private String presetName() { return switch (preset) { case 1 -> "Preview: 4:3 / 320×240"; case 2 -> "Preview: 16:9 / 400×225"; case 3 -> "Preview: ultrawide / 512×216"; default -> "Preview: current window"; }; }
    private static String label(String id) { return id.startsWith("menu.") ? Component.translatable(id).getString() : id.replace("customtitle.", ""); }
    private static Anchor next(Anchor anchor) { Anchor[] values = Anchor.values(); return values[(anchor.ordinal() + 1) % values.length]; }
    private static BackgroundMode next(BackgroundMode mode) { BackgroundMode[] values = BackgroundMode.values(); return values[(mode.ordinal() + 1) % values.length]; }
    private static int nextTint(int tint) { int[] values = {0xFFFFFF, 0xFFE0D0, 0xD0E8FF, 0xD8FFD8, 0xFFD0E8}; for (int i = 0; i < values.length; i++) if (values[i] == tint) return values[(i + 1) % values.length]; return values[0]; }

    private record Preview(int x, int y, int width, int height, int logicalWidth, int logicalHeight) {}
}
