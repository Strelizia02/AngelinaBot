package top.strelitzia.util;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;



@Component
@Slf4j
public class PetPetUtil {
    /**
     * 水平翻转图像
     * @param bufferedImage 目标图像
     * @return 翻转后bufferImage
     */
    public BufferedImage flipImage(final BufferedImage bufferedImage) {
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        BufferedImage img;
        Graphics2D graphics2d;
        (graphics2d = (img = new BufferedImage(w, h, bufferedImage
                .getColorModel().getTransparency())).createGraphics())
                .drawImage(bufferedImage, 0, 0, w, h, w, 0, 0, h, null);
        graphics2d.dispose();
        return img;
    }


    /**
     * 封装图片剪裁方法，防止背景变黑
     * @param outputWidth 输出宽度
     * @param outputHeight 输出高度
     */
    public BufferedImage resizePng(BufferedImage bi2, int outputWidth, int outputHeight) {
        try {
            BufferedImage to = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_USHORT_555_RGB);
            Graphics2D g2d = to.createGraphics();
            to = g2d.getDeviceConfiguration().createCompatibleImage(outputWidth, outputHeight, Transparency.TRANSLUCENT);
            g2d.dispose();
            g2d = to.createGraphics();
            Image from = bi2.getScaledInstance(outputWidth, outputHeight, BufferedImage.SCALE_AREA_AVERAGING);
            g2d.drawImage(from, 0, 0, null);
            g2d.dispose();
            return to;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Image转换成BufferedImage
     * @param image 原始Image
     * @return 返回BufferImage
     */
    public BufferedImage ImageToBufferedImage(Image image)
    {
        BufferedImage  bufferedimage = new BufferedImage
                (image.getWidth(null), image.getHeight(null),BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics g =  bufferedimage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bufferedimage;
    }

    public BufferedImage append(BufferedImage inputImg, BufferedImage outputImg, int x, int y) {
        try {
                    // create basic image
                    Graphics2D g2d = outputImg.createGraphics();
                    BufferedImage imageNew = g2d.getDeviceConfiguration().createCompatibleImage(outputImg.getWidth(), outputImg.getHeight(),
                            Transparency.TRANSLUCENT);
                    g2d.dispose();
                    g2d = imageNew.createGraphics();
                    int oldImgW = outputImg.getWidth();
                    int oldImgH = outputImg.getHeight();
                    g2d.drawImage(outputImg, 0, 0, oldImgW, oldImgH, null);
                    g2d.drawImage(inputImg, x, y, inputImg.getWidth(), inputImg.getHeight(), null);
                    g2d.dispose();
            return imageNew;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 获取一帧的图像
     * @param i 第几帧
     * @param user 用户头像
     * @return 返回当前帧bufferImage
     */
    public BufferedImage getOnePic(int i, BufferedImage user) {
        try {
            int[][] frame_spec = {
                    {27, 31, 86, 90},
                    {22, 36, 91, 90},
                    {18, 41, 95, 90},
                    {22, 41, 91, 91},
                    {27, 28, 86, 91}
                };
            int[] spec = frame_spec[i];
            File file = new File("runFile/petpet/frame" + i + ".png");
            BufferedImage frame = ImageIO.read(file);
            Image userImg = resizePng(flipImage(user), (int)((spec[2] - spec[0]) * 1.2), (int)((spec[3] - spec[1]) * 1.2));
//            File output = new File("F:/frame" + i + ".png");
//            ImageIO.write(ImageToBufferedImage(iframe), "png", output);
            BufferedImage gif_frame = new BufferedImage(112, 112, BufferedImage.TYPE_INT_ARGB_PRE);
            gif_frame = append(ImageToBufferedImage(userImg), gif_frame, spec[0], spec[1]);
            gif_frame = append(frame, gif_frame, 0, 0);
            return gif_frame;
        }catch (IOException e){
            return null;
        }
    }


    /**
     * 把每一帧图像拼接成gif
     * @param user 头像BufferImage
     * @return 返回Gif的BufferImage
     */
    public BufferedImage getGif(String path, BufferedImage user) {
        AnimatedGifEncoder e = new AnimatedGifEncoder();
        e.start(path);
        e.setDelay(100); // 1 frame per sec
        e.setRepeat(0);
        e.setTransparent(new Color(Color.TRANSLUCENT));
        e.setBackground(new Color(Color.TRANSLUCENT));

        for (int i = 0; i < 5; i++){
            BufferedImage onePic = getOnePic(i, user);
            e.addFrame(onePic);
        }
        e.finish();
        try {
            File outFile = new File(path);
            return ImageIO.read(outFile);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
    }

//    public static void main(String[] args) {
//        PetPetUtil pet = new PetPetUtil();
//        ImageUtil imageUtil = new ImageUtil();
//        BufferedImage userImage = ImageUtil.Base64ToImageBuffer(
//                imageUtil.getImageBase64ByUrl("http://q.qlogo.cn/headimg_dl?dst_uin=412459523&spec=100"));
//        File file = new File("F:/a.png");
//        byte[] data;
//        try {
//            ImageIO.write(userImage, "png", file);
//            String path = "frame.gif";
//            pet.getGif(path, userImage);
//            InputStream in = new FileInputStream(path);
//            data = new byte[in.available()];
//            in.read(data);
//            in.close();
//            String base = new BASE64Encoder().encode(Objects.requireNonNull(data));
//            imageUtil.getImgToLocal("F:/", 1, base, "gif");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
