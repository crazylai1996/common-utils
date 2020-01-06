package gdou.laixiaoming.commonutils.image;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * 二维码图片生成
 */
public class QrCodeImage {

    private static final int DEFAULT_SIZE = 50;
    private static final int DEFAULT_MARGIN = 0;
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;


    /**
     * 二维码内容
     */
    private BufferedImage result;
    /**
     * 内容
     */
    private String content;
    /**
     * 大小
     */
    private int size;
    /**
     * 留白
     */
    private int margin;

    private QrCodeImage(String content, int size, int margin){
        this.content = content;
        this.size = size;
        this.margin = margin;
        try {
            this.result = createQrCode();
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建二维码
     * @return
     * @throws WriterException
     */
    private BufferedImage createQrCode() throws WriterException {
        if(StringUtils.isBlank(content)) {
            throw new RuntimeException("二维码内容未设置");
        }
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        //纠错级别
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        //内容所使用字符集编码
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //设置二维码边的空度，非负数
        hints.put(EncodeHintType.MARGIN, margin);
        //要编码的内容
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content,
                //编码类型，目前zxing支持：Aztec 2D,CODABAR 1D format,Code 39 1D,Code 93 1D ,Code 128 1D,
                //Data Matrix 2D , EAN-8 1D,EAN-13 1D,ITF (Interleaved Two of Five) 1D,
                //MaxiCode 2D barcode,PDF417,QR Code 2D,RSS 14,RSS EXPANDED,UPC-A 1D,UPC-E 1D,UPC/EAN extension,UPC_EAN_EXTENSION
                BarcodeFormat.QR_CODE,
                //二维码宽度
                size,
                //二维码高度
                size,
                //生成条形码时的一些配置,此项可选
                hints);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bufferedImage.setRGB(x, y,  (bitMatrix.get(x, y) ? BLACK : WHITE));
            }
        }
        return bufferedImage;
    }

    /**
     * 添加logo
     * @param logoBufferedImage
     * @param size
     */
    public void setLogo(BufferedImage logoBufferedImage, int size) {
        Graphics2D graphics2D = result.createGraphics();
        int x, y;
        //居中
        x = y =(result.getWidth() - size) / 2;
        int width, height;
        width = height = size;
        graphics2D.drawImage(logoBufferedImage, x, y, width, height, null);
        graphics2D.dispose();
    }

    public BufferedImage toBufferedImage() {
        return result;
    }

    public void toFile(String fileName, String formatName, String path) {
        File dest = new File(path, fileName + "." +formatName);
        try {
            ImageIO.write(result, formatName, dest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static QrCodeImageBuilder builder() {
        return QrCodeImageBuilder.aQrCodeImage();
    }

    public static final class QrCodeImageBuilder {
        private String content;
        private int size = DEFAULT_SIZE;
        private int margin = DEFAULT_MARGIN;

        private QrCodeImageBuilder() {
        }

        public static QrCodeImageBuilder aQrCodeImage() {
            return new QrCodeImageBuilder();
        }

        public QrCodeImageBuilder withContent(String content) {
            this.content = content;
            return this;
        }

        public QrCodeImageBuilder withSize(int size) {
            this.size = size;
            return this;
        }

        public QrCodeImageBuilder withMargin(int margin) {
            this.margin = margin;
            return this;
        }

        public QrCodeImage build() {
            return new QrCodeImage(content, size, margin);
        }
    }
}
