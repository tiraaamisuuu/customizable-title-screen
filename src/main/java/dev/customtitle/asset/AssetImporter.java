package dev.customtitle.asset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class AssetImporter {
    private AssetImporter() {}

    public static String importImage(Path source, Path assetDirectory) throws IOException {
        ImageValidator.ImageInfo info = ImageValidator.inspect(source);
        Files.createDirectories(assetDirectory);
        String original = source.getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
        String stem = original.replaceFirst("\\.[^.]+$", "");
        String fileName = stem + "." + (info.extension().equals("jpeg") ? "jpg" : info.extension());
        Path target = assetDirectory.resolve(fileName).normalize();
        if (!target.startsWith(assetDirectory.normalize())) throw new IOException("Unsafe image name");
        int suffix = 2;
        while (Files.exists(target) && !Files.isSameFile(source, target)) {
            fileName = stem + "-" + suffix++ + "." + info.extension();
            target = assetDirectory.resolve(fileName).normalize();
        }
        if (!Files.exists(target) || !Files.isSameFile(source, target)) {
            Path temporary = assetDirectory.resolve(fileName + ".tmp");
            Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }

    public static Path resolveAsset(Path assetDirectory, String relativeName) throws IOException {
        if (relativeName == null || relativeName.isBlank()) throw new IOException("No image selected");
        Path resolved = assetDirectory.resolve(relativeName).normalize();
        if (!resolved.startsWith(assetDirectory.normalize())) throw new IOException("Image path escapes the asset directory");
        return resolved;
    }
}
