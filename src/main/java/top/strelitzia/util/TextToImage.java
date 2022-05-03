package top.strelitzia.util;

import sun.misc.BASE64Encoder;
import top.strelitzia.model.Text;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import static top.strelitzia.util.ImageUtil.replaceEnter;

/**
 * @author wangzy
 * @Date 2021/1/9 21:13
 **/
public class TextToImage {

    public static int[] getWidthAndHeight(Text text, Font font) {
        Rectangle2D r = font.getStringBounds(text.getMaxRow(), new FontRenderContext(
                AffineTransform.getScaleInstance(1, 1), false, false));
        int unitHeight = (int) Math.floor(r.getHeight());//
        int width = (int) Math.round(r.getWidth()) + 2;
        int height = unitHeight * text.getRowsNum();
        System.out.println("width:" + width + ", height:" + height);
        return new int[]{width, height};
    }

    // 根据str,font的样式以及输出base64
    public static String createImage(String text, Font font)
            throws Exception {
        Text t = new Text(text);
        // 获取font的样式应用在str上的整个矩形
        int[] arr = getWidthAndHeight(t, font);
        int width = arr[0];
        int height = arr[1];
        // 创建图片
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_BGR);//创建图片画布
        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE); // 先用白色填充整张图片,也就是背景
        g.fillRect(0, 0, width, height);//画出矩形区域，以便于在矩形区域内写入文字
        g.setColor(Color.black);// 再换成黑色，以便于写入文字
        g.setFont(font);// 设置画笔字体
        String[] rows = t.getText();
        Pattern pattern = Pattern.compile("[0-9]*");
        for (int i = 0; i < t.getRowsNum(); i++) {
            if (rows[i].length() > 0 && pattern.matcher(rows[i].charAt(0) + "").matches()) {
                g.setFont(new Font("楷体", Font.BOLD, font.getSize()));
                g.setColor(Color.BLUE);
                g.drawString(rows[i], 0, (i + 1) * font.getSize() + 1);// 画出一行字符串
                g.setFont(font);
                g.setColor(Color.black);
            } else {
                g.drawString(rows[i], 0, (i + 1) * font.getSize() + 1);// 画出一行字符串
            }
        }
        g.dispose();
        String s = replaceEnter(new BASE64Encoder().encode(imageToBytes(image)));
        return s;
    }

    /**
     * BufferedImage转byte[]
     */
    public static byte[] imageToBytes(BufferedImage bImage) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bImage, "png", out);
        } catch (IOException ignored) {
        }
        return out.toByteArray();
    }
}
