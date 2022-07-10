package top.strelitzia.util;

import com.baidu.aip.client.BaseClient;
import com.baidu.aip.http.AipRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
public class BaiduAPIUtil{

    /**
     * 单例实现
     */
    private static volatile BaiduAPIUtil instance = null;

    public String APP_ID;
    public String API_KEY;
    public String SECRET_KEY;

    public BaiduAPIUtil(String APP_ID, String API_KEY, String SECRET_KEY) {
        this.APP_ID = APP_ID;
        this.API_KEY = API_KEY;
        this.SECRET_KEY = SECRET_KEY;
    }

    public static BaiduAPIUtil getInstance(String APP_ID, String API_KEY, String SECRET_KEY) {
        if (instance == null) {
            synchronized (BaiduAPIUtil.class) {
                if (instance == null) {
                    instance = new BaiduAPIUtil(APP_ID, API_KEY, SECRET_KEY);
                }
            }
        }
        return instance;
    }

    /**
     * 根据图片url，调用百度识图api获取文字列表
     * 白底黑字的截图有可能一行读取为一个单词
     *
     * @param url
     * @return
     */
    public String[] BaiduOCRGetTags(String url) {
        // 初始化一个AipOcr
        MyApiOcr client = new MyApiOcr(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
        // 也可以直接通过jvm启动参数设置此环境变量
        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");

        //只筛选识图的这些词语，其他词语抛弃
        String[] all = {"远程位", "治疗", "支援机械", "医疗干员", "近战位", "支援", "近卫干员", "新手"
                , "先锋干员", "术师干员", "狙击干员", "重装干员", "费用回复", "输出", "群攻", "减速", "辅助干员"
                , "生存", "防护", "削弱", "快速复活", "特种干员", "位移", "资深干员", "高级资深干员", "爆发", "控场", "召唤"};
        List<String> allTag = Arrays.asList(all);

        // 调用接口
        JSONObject res = client.basicAccurateGeneral(url, new HashMap<>());
        JSONArray words_result = new JSONArray(res.get("words_result").toString());
        List<String> str = new ArrayList<>();
        for (int i = 0; i < words_result.length(); i++) {
            String words = words_result.getJSONObject(i).getString("words");
            if (allTag.contains(words)) {
                str.add(words);
            }
        }
        String[] s = new String[str.size()];
        str.toArray(s);
        return s;
    }

//    public byte[] base64ToImgByteArray(String base64) throws IOException {
//        sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
//        //因为参数base64来源不一样，需要将附件数据替换清空掉。如果此入参来自canvas.toDataURL("image/png");
//        base64 = base64.replaceAll("data:image/png;base64,", "");
//        //base64解码并转为二进制数组
//        byte[] bytes = decoder.decodeBuffer(base64);
//        for (int i = 0; i < bytes.length; ++i) {
//            if (bytes[i] < 0) {// 调整异常数据
//                bytes[i] += 256;
//            }
//        }
//        return bytes;
//    }
}

class MyApiOcr extends BaseClient{
    /**
     * 我他妈直接重写百度的高精度sdk方法
     * @param appId
     * @param apiKey
     * @param secretKey
     */
    protected MyApiOcr(String appId, String apiKey, String secretKey) {
        super(appId, apiKey, secretKey);
    }

    public JSONObject basicAccurateGeneral(String url, HashMap<String, String> options) {
        AipRequest request = new AipRequest();
        preOperation(request);

        request.addBody("url", url);
        if (options != null) {
            request.addBody(options);
        }
        request.setUri("https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic");
        postOperation(request);
        return requestServer(request);
    }
}
