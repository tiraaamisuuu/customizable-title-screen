package dev.customtitle.asset;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;

public final class ImageValidator {
    public static final long MAX_BYTES = 64L * 1024 * 1024;
    public static final int MAX_DIMENSION = 8192;
    public static final long MAX_PIXELS = 33_554_432L;

    private ImageValidator() {}

    public static ImageInfo inspect(Path path) throws IOException {
        if (!Files.isRegularFile(path)) throw new IOException("Image does not exist");
        long bytes = Files.size(path);
        if (bytes <= 0 || bytes > MAX_BYTES) throw new IOException("Image file is empty or larger than 64 MiB");
        String extension = extension(path);
        if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("jpeg")) throw new IOException("Only PNG and JPEG images are supported");
        try (ImageInputStream stream = ImageIO.createImageInputStream(path.toFile())) {
            if (stream == null) throw new IOException("Image could not be opened");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new IOException("Unsupported or corrupt image");
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > MAX_DIMENSION || height > MAX_DIMENSION || (long) width * height > MAX_PIXELS) {
                    throw new IOException("Image dimensions are unsafe (maximum 8192 per side and 32 megapixels)");
                }
                return new ImageInfo(width, height, bytes, extension);
            } finally {
                reader.dispose();
            }
        }
    }

    private static String extension(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public record ImageInfo(int width, int height, long bytes, String extension) {}
}
