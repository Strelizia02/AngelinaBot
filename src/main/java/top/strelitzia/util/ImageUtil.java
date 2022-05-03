package top.strelitzia.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
@Component
@Slf4j
public class ImageUtil {
    /**
     * 根据url下载文件到本地，返回本地路径
     *
     * @param imgUrl
     * @return
     */
    public String getImageLocalPathFromUrl(String imgUrl) {
        String dir = "/root/img/";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        URL url;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String imageName = sdf.format(new Date()) + ".jpg";
        String path = dir + imageName;
        InputStream is = null;
        FileOutputStream outStream = null;
        HttpURLConnection httpUrl = null;
        try {
            url = new URL(imgUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            httpUrl.getInputStream();
            is = httpUrl.getInputStream();
            outStream = new FileOutputStream(new File(path));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            log.info("{}图片下载成功", imageName);
            return path;
        } catch (Exception e) {
            log.info("图片下载失败");
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpUrl != null) {
                httpUrl.disconnect();
            }
        }
        return imgUrl;
    }

    /**
     * 通过图片的url获取图片的base64字符串
     *
     * @param imgUrl 图片url
     * @return 返回图片base64的字符串
     */
    public static String getImageBase64ByUrl(String imgUrl) {
        URL url;
        InputStream is = null;
        ByteArrayOutputStream outStream = null;
        HttpURLConnection httpUrl = null;
        try {
            url = new URL(imgUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.setRequestProperty("User-Agent", "PostmanRuntime/7.26.8");
            httpUrl.setRequestProperty("Authorization", "2");
            httpUrl.setRequestProperty("Host", "andata.somedata.top");
            httpUrl.connect();
            httpUrl.getInputStream();
            is = httpUrl.getInputStream();
            outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            return encode(outStream.toByteArray());
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpUrl != null) {
                httpUrl.disconnect();
            }
        }
        return imgUrl;
    }

    /**
     * 图片转字符串
     *
     * @param image
     * @return
     */
    public static String encode(byte[] image) {
        BASE64Encoder decoder = new BASE64Encoder();
        return replaceEnter(decoder.encode(image));
    }

    public static String replaceEnter(String str) {
        String reg = "[\n-\r]";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(str);
        return m.replaceAll("");
    }

    /**
     * 测试base64转图片，并存储到本地
     *
     * @param base64 base64字符串
     */
    public void getImgToLocal(String dir, Integer id, String base64, String type) {
        if (base64 == null) // 图像数据为空
            return;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            // Base64解码
            byte[] bytes = decoder.decodeBuffer(base64);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {// 调整异常数据
                    bytes[i] += 256;
                }
            }
            String imgFilePath = dir + id + "." + type;
            // 生成jpeg图片
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(bytes);
            out.flush();
            out.close();
        } catch (Exception ignored) {
        }
    }

    public static BufferedImage Base64ToImageBuffer(String base64) {
        if (base64 == null) // 图像数据为空
            return null;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            // Base64解码
            byte[] bytes = decoder.decodeBuffer(base64);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {// 调整异常数据
                    bytes[i] += 256;
                }
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return ImageIO.read(bais);
        } catch (Exception ignored) {
        }
        return null;
    }
}
