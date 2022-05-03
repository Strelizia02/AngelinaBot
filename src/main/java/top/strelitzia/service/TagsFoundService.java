package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.dao.AgentTagsMapper;
import top.strelitzia.model.AgentTagsInfo;
import top.strelitzia.util.BaiduAPIUtil;
import top.strelitzia.util.FormatStringUtil;
import top.strelitzia.util.TagsUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

/**
 * @author wangzy
 * @Date 2020/12/15 10:41
 **/
@Service
@Slf4j
public class TagsFoundService {

    @Autowired
    private AgentTagsMapper agentTagsMapper;

    @Value("${baiduConfig.APP_ID}")
    private String APP_ID;

    @Value("${baiduConfig.API_KEY}")
    private String API_KEY;

    @Value("${baiduConfig.SECRET_KEY}")
    private String SECRET_KEY;

    @AngelinaGroup(keyWords = {"公招截图", "公招", "公开招募"}, dHash = {"0001111110100110001111010010001100100011001001110010011100101101", "0001111101100111001101010110001101110011001001110010011100101111"}, description = "查询公招结果")
    public ReplayInfo FoundAgentByJson(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        Map<List<String>, List<AgentTagsInfo>> listListMap = FoundTagsByImg(messageInfo.getImgUrlList().get(0));
        replayInfo.setReplayImg(MapToBase64(listListMap));
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"公招文字", "公招tag"}, description = "通过文字方式访问公招结果，用逗号分割")
    public ReplayInfo FoundAgentByArray(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            Map<List<String>, List<AgentTagsInfo>> listMap = FoundTagResultByArrays(messageInfo.getArgs().get(1).split(",|，"));
            replayInfo.setReplayImg(MapToBase64(listMap));
        } else {
            replayInfo.setReplayMessage("请输入公招tag，用逗号分割");
        }
        return replayInfo;
    }

    public Map<List<String>, List<AgentTagsInfo>> FoundTagsByImg(String url) {
        //单例模式的百度API实例
        BaiduAPIUtil baiduAPIUtil = BaiduAPIUtil.getInstance(APP_ID, API_KEY, SECRET_KEY);

        //调用百度api图片识别
        String[] s = baiduAPIUtil.BaiduOCRGetTags(url);
        log.info("识图获取到tag为：{}", Arrays.asList(s));

        return FoundTagResultByArrays(s);
    }

    public Map<List<String>, List<AgentTagsInfo>> FoundTagResultByArrays(String[] s) {

        //把数组进行笛卡尔积组合
        List<List<String>> allCompose = TagsUtil.getAllCompose(Arrays.asList(s));

        //用于保存结果
        Map<List<String>, List<AgentTagsInfo>> result = new HashMap<>();

        //遍历所有的组合
        for (List<String> list : allCompose) {
            //没有tag的时候跳出循环
            if (list.size() == 0) {
                continue;
            }
            if (TagsUtil.isHave(list, "高级资深干员")) {
                result.put(list, agentTagsMapper.selectSixAgentByTag(list));
            } else {
                result.put(list, agentTagsMapper.selectAgentByTag(list));
            }
        }
        return result;
    }

    //把Map转换成特定格式字符串
    public String MapToString(Map<List<String>, List<AgentTagsInfo>> map) {
        //保存结果
        StringBuilder s = new StringBuilder();
        //循环遍历Map
        for (Map.Entry<List<String>, List<AgentTagsInfo>> m : map.entrySet()) {
            //获取到Key，Value
            List<String> key = m.getKey();
            List<AgentTagsInfo> value = m.getValue();
            //如果组合中没有值，或者组合中不能确定稀有干员，则直接进行下次循环
            if (value.size() == 0 || isThreeOrTwoInMap(value)) continue;
            StringBuilder tags = new StringBuilder();
            StringBuilder agents = new StringBuilder();
            //用key的标签组合作为head
            for (String tag : key) {
                tags.append(",").append(tag);
            }
            //用干员名字+星星的列表作为body
            for (AgentTagsInfo agent : value) {
                String levelStar;
                Integer star = agent.getStar();
                //不要三星两星的结果
                if (star == 3 || star == 2) break;
                levelStar = FormatStringUtil.FormatStar(star);
                agents.append("\n").append(agent.getAgentName()).append(levelStar);
            }
            s.append("\n\n").append(tags.substring(1)).append("\n").append(agents.substring(1));
        }
        if (s.toString().equals("")) {
            s = new StringBuilder("\t\tQAQ没有找到对应的稀有公招结果");
        }
        return s.substring(2);
    }

    //把Map转换成图片base64
    public BufferedImage MapToBase64(Map<List<String>, List<AgentTagsInfo>> map) {
        //保存结果
        int height = 0;
        List<BufferedImage> imagesList = new ArrayList<>();
        boolean isReturn = false;
        //循环遍历Map
        for (Map.Entry<List<String>, List<AgentTagsInfo>> m : map.entrySet()) {
            //获取到Key，Value
            StringBuilder key = new StringBuilder("[ ");
            m.getKey().forEach(str -> key.append(str).append(","));
            key.deleteCharAt(key.length() - 1);
            key.append(" ]");
            List<AgentTagsInfo> value = m.getValue();
            int minStar = getAgentListMinStar(value);
            BufferedImage pic = drawPicByAgentList(key.toString(), value, minStar);
            if(pic != null) {
                isReturn = true;
                imagesList.add(pic);
            }
        }
        if (!isReturn){
            return null;
        }
        int maxHeight = 0;
        for(BufferedImage bf: imagesList){
            maxHeight += bf.getHeight() + 1;
        }
        BufferedImage image = new BufferedImage(1250, maxHeight + 10, BufferedImage.TYPE_INT_BGR);
        Font font = new Font("楷体", Font.BOLD, 50);
        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE); // 先用白色填充整张图片,也就是背景
        g.fillRect(0, 0, 1250, maxHeight + 10);//画出矩形区域，以便于在矩形区域内写入文字
        g.setFont(font);// 设置画笔字体
        for(BufferedImage bf: imagesList){
            if (bf != null) {
                g.drawImage(bf, 0, height, null);
                height += bf.getHeight();
            }
        }
        g.dispose();
        return image;
    }

    /**
     * 画出一个组合的结果
     * @param key
     * @param value
     * @param star
     * @return
     */
    private BufferedImage drawPicByAgentList(String key, List<AgentTagsInfo> value, int star){
        if(star < 4 || value.size() == 0){
            return null;
        }
        Map<Integer, List<String>> integerListMap = groupByStar(value);
        int height = 0;
        for(List<String> list:integerListMap.values()){
            height += list.size() + 1;
        }
        int length = 0;
        BufferedImage image = new BufferedImage(1250, (height + 1) * 50 + 10, BufferedImage.TYPE_INT_BGR);
        Font font = new Font("楷体", Font.BOLD, 50);
        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE); // 先用白色填充整张图片,也就是背景
        g.fillRect(0, 0, 1250, (height + 1) * 50 + 10);//画出矩形区域，以便于在矩形区域内写入文字
        g.setColor(new Color(70,130,180));// 再换成黑色，以便于写入文字
        g.setFont(font);// 设置画笔字体
        g.drawString(key, 0, 50);
        length++;
        if (integerListMap.containsKey(6)){
            g.setColor(Color.RED);
            g.drawString("  ★★★★★★", 0, 50 + 50 * length);
            length++;
            g.setColor(Color.BLACK);
            for (String line: integerListMap.get(6)){
                g.drawString("    " + line, 0, 50 + 50 * length);
                length++;
            }
        }
        if (integerListMap.containsKey(5)){
            g.setColor(Color.ORANGE);
            g.drawString("  ★★★★★", 0, 50 + 50 * length);
            length++;
            g.setColor(Color.BLACK);
            for (String line: integerListMap.get(5)){
                g.drawString("    " + line, 0, 50 + 50 * length);
                length++;
            }
        }
        if (integerListMap.containsKey(4)){
            g.setColor(Color.MAGENTA);
            g.drawString("  ★★★★", 0, 50 + 50 * length);
            length++;
            g.setColor(Color.BLACK);
            for (String line: integerListMap.get(4)){
                g.drawString("    " + line, 0, 50 + 50 * length);
                length++;
            }
        }
        if (integerListMap.containsKey(1)){
            g.setColor(Color.GRAY);
            g.drawString("  ★", 0, 50 + 50 * length);
            length++;
            g.setColor(Color.BLACK);
            for (String line: integerListMap.get(1)){
                g.drawString("    " + line, 0, 50 + 50 * length);
                length++;
            }
        }
        g.dispose();
        return image;
    }

    /**
     * 生成一个star,lines的键值对，List每个元素对应一行（5个干员）
     * @param value
     * @return
     */
    private Map<Integer, List<String>> groupByStar(List<AgentTagsInfo> value){
        Map<Integer, List<String>> result = new HashMap<>();
        List<String> six = new ArrayList<>();
        List<String> five = new ArrayList<>();
        List<String> four = new ArrayList<>();
        List<String> seven = new ArrayList<>();
        StringBuilder sixSb = new StringBuilder();
        StringBuilder fiveSb = new StringBuilder();
        StringBuilder fourSb = new StringBuilder();
        StringBuilder sevenSb = new StringBuilder();
        int i = 0;
        int j = 0;
        int k = 0;
        int m = 0;
        for(AgentTagsInfo a: value){
            if(a.getStar() == 6){
                if (i == 5) {
                    sixSb.deleteCharAt(sixSb.length() - 1);
                    six.add(sixSb.toString());
                    sixSb.delete(0, sixSb.length());
                    i = 0;
                }
                sixSb.append(a.getAgentName()).append(",");
                i++;
            }else if(a.getStar() == 5){
                if (j == 5) {
                    fiveSb.deleteCharAt(fiveSb.length() - 1);
                    five.add(fiveSb.toString());
                    fiveSb.delete(0, fiveSb.length());
                    j = 0;
                }
                fiveSb.append(a.getAgentName()).append(",");
                j++;
            }else if(a.getStar() == 4) {
                if (k == 5) {
                    fourSb.deleteCharAt(fourSb.length() - 1);
                    four.add(fourSb.toString());
                    fourSb.delete(0, fourSb.length());
                    k = 0;
                }
                fourSb.append(a.getAgentName()).append(",");
                k++;
            }else if(a.getStar() == 1) {
                if (m == 5) {
                    sevenSb.deleteCharAt(sevenSb.length() - 1);
                    seven.add(sevenSb.toString());
                    sevenSb.delete(0, sevenSb.length());
                    k = 0;
                }
                sevenSb.append(a.getAgentName()).append(",");
                m++;
            }
        }
        if (fourSb.length() > 0) {
            fourSb.deleteCharAt(fourSb.length() - 1);
            four.add(fourSb.toString());
        }
        if (fiveSb.length() > 0) {
            fiveSb.deleteCharAt(fiveSb.length() - 1);
            five.add(fiveSb.toString());
        }
        if (sixSb.length() > 0) {
            sixSb.deleteCharAt(sixSb.length() - 1);
            six.add(sixSb.toString());
        }
        if (sevenSb.length() > 0) {
            sevenSb.deleteCharAt(sevenSb.length() - 1);
            seven.add(sevenSb.toString());
        }

        if (six.size() > 0){
            result.put(6, six);
        }
        if (five.size() > 0){
            result.put(5, five);
        }
        if (four.size() > 0){
            result.put(4, four);
        }
        if (seven.size() > 0){
            result.put(1, seven);
        }
        return result;
    }

    private int getAgentListMinStar(List<AgentTagsInfo> list){
        //判断干员结果集中的最小干员
        int result = 6;
        if (list.size() == 0){
            return 0;
        }
        for (AgentTagsInfo a: list){
            int star = a.getStar();
            if (star == 1){
                star = 7;
            }
            if (star < result){
                result = star;
            }
        }
        return result;
    }

    //判定里面是否有三星/两星
    public boolean isThreeOrTwoInMap(List<AgentTagsInfo> map) {
        for (AgentTagsInfo a : map) {
            Integer star = a.getStar();
            if (star == 3 || star == 2) {
                return true;
            }
        }
        return false;
    }
}
