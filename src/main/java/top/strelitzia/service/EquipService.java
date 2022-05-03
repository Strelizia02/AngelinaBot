package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.dao.EquipMapper;
import top.strelitzia.dao.MaterialMadeMapper;
import top.strelitzia.dao.NickNameMapper;
import top.strelitzia.model.EquipBuff;
import top.strelitzia.model.EquipInfo;
import top.strelitzia.model.MaterialInfo;
import top.strelitzia.model.Text;
import top.strelitzia.util.ImageUtil;
import top.strelitzia.util.TextToImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class EquipService {

    @Autowired
    private EquipMapper equipMapper;

    @Autowired
    private MaterialMadeMapper materialMadeMapper;

    @Autowired
    private NickNameMapper nickNameMapper;

    @AngelinaGroup(keyWords = {"模组查询", "查询模组", "模组"}, description = "查询模组信息")
    public ReplayInfo getOperatorEquip(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String name = messageInfo.getArgs().get(1);
            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals(""))
                name = realName;

            EquipInfo equipInfo = equipMapper.selectEquipByName(name);
            if (equipInfo != null) {
                String equipId = equipInfo.getEquipId();
                List<EquipBuff> equipBuffs = equipMapper.selectEquipBuffById(equipId);
                List<MaterialInfo> materialInfos = equipMapper.selectEquipCostById(equipId);
                List<String> strings = equipMapper.selectEquipMissionById(equipId);
                StringBuilder s = new StringBuilder("");
                s.append("干员").append(name).append("的模组信息为：\n")
                        .append("  模组名称： ").append(equipInfo.getEquipName()).append("\n")
                        .append("  模组特性： ").append(equipInfo.getDesc()).append("\n")
                        .append("  解锁等级： 精英化").append(equipInfo.getPhase()).append(" ").append(equipInfo.getLevel()).append("级\n");
                int i = 1;
                s.append("  解锁条件：\n");
                for (String mission : strings) {
                    s.append("  ").append(i).append(".").append(mission).append("\n");
                    i++;
                }
                for (EquipBuff e : equipBuffs) {
                    String value = "";
                    if (e.getValue() >= 0) {
                        value = "+" + e.getValue();
                    } else {
                        value = "-" + e.getValue();
                    }
                    s.append("  面板变化： ").append(returnBuffName(e.getBuffName())).append(" ").append(value).append("\n");
                }
                s.append("  解锁材料： \n");
                try {
                    replayInfo.setReplayImg(sendImageWithPic(materialInfos, s.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return replayInfo;
            } else {
                replayInfo.setReplayMessage("未找到干员对应模组信息");
            }
        } else {
            replayInfo.setReplayMessage("请输入需要查询的干员名称");
        }
        return replayInfo;
    }

    public String returnBuffName(String BuffId){
        Map<String, String> map = new HashMap<>();
        map.put("max_hp", "生命上限");
        map.put("atk", "攻击");
        map.put("def", "防御");
        map.put("magicResistance", "法术抵抗");
        map.put("cost", "部署费用");
        map.put("blockCnt", "阻挡数");
        map.put("baseAttackTime", "攻击间隔");
        map.put("respawnTime", "再部署");
        map.put("attack_speed", "攻击速度");

        if (map.get(BuffId) != null) {
            return map.get(BuffId);
        }
        else {
            return BuffId;
        }
    }

    public BufferedImage sendImageWithPic(List<MaterialInfo> materialInfos, String s) throws Exception {
        Text t = new Text(s);
        Font font = new Font("楷体", Font.PLAIN, 100);

        // 获取font的样式应用在str上的整个矩形
        int[] arr = TextToImage.getWidthAndHeight(t, font);
        int width = arr[0];
        int height = arr[1] + 100 * materialInfos.size() + 120;
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_BGR);//创建图片画布
        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE); // 先用白色填充整张图片,也就是背景
        g.fillRect(0, 0, width, height);//画出矩形区域，以便于在矩形区域内写入文字
        g.setColor(Color.black);// 再换成黑色，以便于写入文字
        g.setFont(font);// 设置画笔字体
        String[] rows = t.getText();
        Pattern pattern = Pattern.compile("[0-9]*");
        //记录画笔高度
        int gHeight = 0;
        for (int i = 0; i < t.getRowsNum(); i++) {
            gHeight = (i + 1) * font.getSize() + 1;
            if (rows[i].length() > 0 && pattern.matcher(rows[i].charAt(0) + "").matches()) {
                g.setFont(new Font("楷体", Font.BOLD, font.getSize()));
                g.setColor(Color.BLUE);
                g.drawString(rows[i], 0, gHeight);// 画出一行字符串
                g.setFont(font);
                g.setColor(Color.black);
            } else {
                g.drawString(rows[i], 0, gHeight);// 画出一行字符串
            }
        }
        for (MaterialInfo m : materialInfos) {
            String imgBase64 = materialMadeMapper.selectMaterialPicByName(m.getMaterialName());
            g.drawImage(ImageUtil.Base64ToImageBuffer(imgBase64), 200, gHeight, 100, 100, null);// 画出材料图标
            g.drawString(m.getMaterialName() + " * " + m.getMaterialNum() + "个", 300, gHeight + font.getSize());
            gHeight += font.getSize();
        }
        g.dispose();
        return image;
    }
}
