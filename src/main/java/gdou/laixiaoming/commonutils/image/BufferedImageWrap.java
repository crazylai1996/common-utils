package gdou.laixiaoming.commonutils.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 图片包装
 */
public abstract class BufferedImageWrap {

    public abstract BufferedImage getRealImage();

    public BufferedImage toBufferedImage() {
        return getRealImage();
    }

    public void toFile(String fileName, String formatName, String path) {
        File dest = new File(path, fileName + "." +formatName);
        try {
            ImageIO.write(getRealImage(), formatName, dest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] toByteArray() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(getRealImage(), "png", out);
            return out.toByteArray();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
