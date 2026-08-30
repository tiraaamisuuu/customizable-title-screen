# Customizable Title Screen

Customizable Title Screen is a strictly client-side Fabric mod for Minecraft Java Edition 26.2. It turns the title screen into a responsive layout editor: import a local image, choose how it fits, and arrange title-screen controls without hand-editing JSON.

## Requirements

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3+
- Fabric API 0.158.0+26.2 (or a compatible newer 26.2 build)
- Java 25
- Mod Menu 20.0.1 is optional and adds the editor entry when installed.

The mod has a client environment entrypoint and is not required by servers.

## Install and use

1. Put the release JAR in the instance `mods` folder with Fabric Loader and Fabric API.
2. Choose **Customize Title Screen** from the title screen (or from Mod Menu).
3. In **Background**, click **Import Folder**, place a PNG/JPEG in `config/customizable-title-screen/assets`, then click the background selector to cycle through validated images. Nothing is uploaded or read from the network.
4. Choose Cover, Contain, Stretch, Center, or Tile and adjust dimming, blur, tint, and opacity.
5. In **Layout**, drag an element in the preview. Center/edge snap guides are active by default; hold Shift to disable snapping. Use the controls to change anchor, visibility, size, and scale.
6. **Save** persists changes, **Apply** previews them for the current session, and **Cancel** restores the state from when the editor opened.

The preview cycles through the current window, 4:3, 16:9, and ultrawide layouts. Unknown widgets added by another mod remain at their vanilla position.

## Storage and profiles

Files live under `config/customizable-title-screen/`:

- `config.json` — versioned, readable profiles
- `config.json.bak` — last-known-good backup used after a failed parse/interrupted write
- `assets/` — local PNG/JPEG backgrounds
- `active-profile.json` — written by **Export active-profile.json**
- `import-profile.json` — read by **Import import-profile.json**

Writes use a temporary file and atomic move where supported. Imported JSON is data only, never executed; asset paths cannot escape the asset directory. Profiles can be duplicated and switched from **Profiles & Files**. Schema 1 files migrate automatically; newer/invalid schemas fall back to the backup/default layout.

## Emergency access

- `Ctrl` + `Alt` + `C` on the title screen opens the editor.
- `Ctrl` + `Alt` + `Shift` + `C` resets the active profile and saves safe defaults.

## Compatibility and limitations

The mod uses narrow title-screen/screen-event mixins and does not replace unrelated screens or touch multiplayer behavior. Minecraft 26.2's render-state extraction API is used for the custom image. Blur is generated once when an image/effect setting changes, not per frame, and runtime GPU textures are released when title/editor screens change. Logo and edition rendering are handled as one safe title group; third-party widgets are not forcibly reordered. A conflicting title-screen mod may take precedence.

### Legacy 1.16.5 build

A separate lightweight compatibility project is included at `compat/1.16.5`. Its jar supports Fabric Loader 0.11.x/Fabric API 0.42 on Minecraft 1.16.5 and provides the local PNG/JPEG background, a draggable button-preview editor, and reset/save controls. It uses the older 1.16 rendering and screen APIs, so it is not feature-identical to the 26.2 build. Build it with Java 17 from that directory:

```text
./gradlew build
```

The legacy distributable is `compat/1.16.5/build/libs/customizable-title-screen-1.16.5-1.0.0.jar`. Other Minecraft versions require their own mapped compatibility build; do not install the 26.2 jar on older versions.

## Build

Use the included Gradle wrapper with Java 25:

```text
./gradlew build
```

The distributable JAR is written to `build/libs/customizable-title-screen-1.0.0.jar`. Generated `build/`, `.gradle/`, run data, imported images, and local paths are ignored by Git.

## Manual QA checklist

- [ ] Fresh instance reaches the title screen with only Fabric Loader/API and this JAR.
- [ ] Mod Menu opens the same editor when installed; the mod still loads without it.
- [ ] Test GUI scales 1–4, 4:3, 16:9, and ultrawide windows; essential navigation stays reachable.
- [ ] Import valid PNG/JPEG, missing/corrupt/oversized image, and unsupported extension.
- [ ] Cycle display modes; change blur/tint/opacity and confirm no per-frame log spam.
- [ ] Drag, resize, reanchor, hide, undo/redo, and reload every known widget.
- [ ] Switch/duplicate/export/import profiles and interrupt a config write; confirm backup recovery.
- [ ] Reload resources and repeatedly open/close title/editor; confirm textures do not accumulate.
- [ ] Install alongside a title-screen-changing mod; confirm unknown widgets fall back safely.

## Screenshots

Screenshots can be added for release builds. The repository intentionally contains no imported user images or generated captures.

## License

MIT — see [LICENSE](LICENSE).
