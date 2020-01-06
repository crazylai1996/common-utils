package gdou.laixiaoming.commonutils.image;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * 缩略图
 */
public class ThumbnailImage extends BufferedImageWrap{

    private BufferedImage thumbnail;

    private ThumbnailImage() {

    }

    @Override
    public BufferedImage getRealImage() {
        return thumbnail;
    }

    public static ThumbnailImage of(BufferedImage src,
                                    int thumbWidth, int thumbHeight,
                                    float quality){
        ThumbnailImage thumbnailImage = new ThumbnailImage();
        try {
            thumbnailImage.thumbnail = createThumbnail(src, thumbWidth, thumbHeight, quality);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        return thumbnailImage;
    }

    private static BufferedImage createThumbnail(BufferedImage src,
                                          int thumbWidth, int thumbHeight,
                                          float quality) throws IOException {
        //调整缩略图大小
        double thumbnailRatio = (double)thumbWidth / (double)thumbHeight;
        int imageWidth = src.getWidth(null);
        int imageHeight = src.getHeight(null);
        double srcRatio = (double)imageWidth / (double)imageHeight;
        if(thumbnailRatio < srcRatio){
            thumbHeight = (int)(thumbWidth / srcRatio);
        }else{
            thumbWidth = (int)(thumbHeight * srcRatio);
        }

        //缩放
        BufferedImage scaledImage = new BufferedImage(thumbWidth, thumbHeight,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2d = scaledImage.createGraphics();
        graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2d.drawImage(src, 0, 0, thumbWidth, thumbHeight,null);
        graphics2d.dispose();

        //压缩
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(baos);
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(scaledImage);
        quality = Math.max(0, Math.min(quality, 1));
        param.setQuality(quality, false);
        encoder.setJPEGEncodeParam(param);
        encoder.encode(scaledImage);
        out.close();
        ByteArrayInputStream bios = new ByteArrayInputStream(baos.toByteArray());
        BufferedImage result = ImageIO.read(bios);
        return result;
    }
}
