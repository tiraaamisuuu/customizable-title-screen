package dev.customtitle.asset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageValidatorTest {
    @TempDir Path temporary;

    @Test
    void acceptsPngAndReportsDimensions() throws IOException {
        Path image = temporary.resolve("valid.png");
        ImageIO.write(new BufferedImage(32, 24, BufferedImage.TYPE_INT_ARGB), "png", image.toFile());
        ImageValidator.ImageInfo info = ImageValidator.inspect(image);
        assertEquals(32, info.width());
        assertEquals(24, info.height());
    }

    @Test
    void rejectsCorruptAndUnsupportedFiles() throws IOException {
        Path corrupt = temporary.resolve("corrupt.jpg");
        Files.writeString(corrupt, "not an image");
        assertThrows(IOException.class, () -> ImageValidator.inspect(corrupt));
        Path unsupported = temporary.resolve("image.gif");
        Files.write(unsupported, new byte[]{1, 2, 3});
        assertThrows(IOException.class, () -> ImageValidator.inspect(unsupported));
    }

    @Test
    void assetResolutionCannotEscapeFolder() {
        assertThrows(IOException.class, () -> AssetImporter.resolveAsset(temporary, "../secret.png"));
    }
}
