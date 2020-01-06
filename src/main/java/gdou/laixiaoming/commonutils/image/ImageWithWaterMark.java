package gdou.laixiaoming.commonutils.image;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 图片带水印
 */
public class ImageWithWaterMark extends BufferedImageWrap{

    private static final String DEFALUT_FONT_FAMILY = "微软雅黑";
    private static final int DEFALUT_FONT_STYLE = Font.BOLD;
    private static final int DEFALUT_FONT_SIZE = 24;
    private static final float DEFALUT_ALPHA = 0.7F;
    private static final int DEFALUT_LOGO_WIDTH = 100;

    private String fontFamily = DEFALUT_FONT_FAMILY;
    private int fontStyle = DEFALUT_FONT_STYLE;
    private int fontSize = DEFALUT_FONT_SIZE;
    private float alpha = DEFALUT_ALPHA;
    private int logoWidth = DEFALUT_LOGO_WIDTH;

    private String textContent;
    private BufferedImage logo;

    private BufferedImage src;
    private BufferedImage result;

    @Override
    public BufferedImage getRealImage() {
        if(result == null) {
            if(textContent != null) {
                result = createWithTextMark();
            }else if(logo != null){
                result = createWithLogoMark();
            }
        }
        return result;
    }

    public static ImageWithWaterMark ofTextWaterMark(BufferedImage src, String textContent) {
        ImageWithWaterMark imageWithWaterMark = new ImageWithWaterMark();
        imageWithWaterMark.src = src;
        imageWithWaterMark.textContent = textContent;
        return imageWithWaterMark;
    }

    public static ImageWithWaterMark ofImageWaterMark(BufferedImage src, BufferedImage logo) {
        ImageWithWaterMark imageWithWaterMark = new ImageWithWaterMark();
        imageWithWaterMark.src = src;
        imageWithWaterMark.logo = logo;
        return imageWithWaterMark;
    }

    public ImageWithWaterMark setFont(String fontFamily,
                        int fontStyle,
                        int fontSize,
                        float alpha) {
        this.fontFamily = fontFamily;
        this.fontStyle = fontStyle;
        this.fontSize = fontSize;
        this.alpha = alpha;
        return this;
    }

    /**
     * 文字水印
     * @return
     * @throws Exception
     */
    private BufferedImage createWithTextMark(){
        int width = src.getWidth(null);
        int height = src.getHeight(null);

        BufferedImage tarBuffImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = tarBuffImage.createGraphics();
        g.drawImage(src, 0, 0, width,height,null);

        //计算
        int strWidth = fontSize * getTextLength(textContent);
        int strHeight = fontSize;

        int x=0, y=0;

        //设置字体和水印透明度
        g.setFont(new Font(fontFamily, fontStyle, fontSize));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        //旋转图片
        g.rotate(Math.toRadians(-30),width / 2,height / 2);
        while(x < width){
            y = 0;
            while(y < height){
                g.drawString(textContent, x, y);
                y += (strHeight + 50);
            }
            //水印之间的间隔设为50
            x += (strWidth + 50);
        }
        g.dispose();

        return tarBuffImage;
    }

    /**
     * 图片水印
     * @return
     * @throws Exception
     */
    private BufferedImage createWithLogoMark(){
        int width = src.getWidth(null);
        int height = src.getHeight(null);

        BufferedImage tarBuffImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = tarBuffImage.createGraphics();
        g.drawImage(src, 0, 0, width, height,null);

        int logoHeight = (logoWidth * logo.getHeight(null)) / logo.getWidth(null);

        int x = 0, y = 0;

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        //旋转图片
        g.rotate(Math.toRadians(-30),width / 2,height / 2);
        while(x < width){
            y = 0;
            while(y < height){
                g.drawImage(logo, x, y, logoWidth, logoHeight, null);
                y += (logoHeight + 50);
            }
            //水印之间的间隔设为50
            x += (logoWidth + 50);
        }
        g.dispose();

        return tarBuffImage;
    }

    /**
     * 文本长度的处理：文字水印的中英文字符的宽度转换
     * @param text
     * @return
     */
    private static int getTextLength(String text){
        int length = text.length();
        for(int i=0; i < text.length(); i++){
            String s = String.valueOf(text.charAt(i));
            //中文字符
            if(s.getBytes().length > 1){
                length++;
            }
        }
        //中文和英文字符的转换
        length = length % 2 == 0 ? length / 2 : length / 2 + 1;
        return length;
    }
}
