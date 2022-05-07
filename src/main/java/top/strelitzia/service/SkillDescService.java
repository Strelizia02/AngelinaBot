package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.model.TextLine;
import top.strelitzia.dao.NickNameMapper;
import top.strelitzia.arknightsDao.SkillDescMapper;
import top.strelitzia.model.SkillDesc;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
@Service
public class SkillDescService {

    @Autowired
    private SkillDescMapper skillDescMapper;

    @Autowired
    private NickNameMapper nickNameMapper;

    @AngelinaGroup(keyWords = {"技能详情", "技能描述", "技能"}, description = "查询技能详情")
    public ReplayInfo getSkillDescByInfo(MessageInfo messageInfo) throws IOException {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            Map<Integer, String> spType = new HashMap<>();
            Map<Integer, String> skillType = new HashMap<>();
            Map<String, String> levelStr = new HashMap<>();

            levelStr.put("专一", "8");
            levelStr.put("专二", "9");
            levelStr.put("专三", "10");

            spType.put(1, "自动回复");
            spType.put(2, "攻击回复");
            spType.put(4, "受击回复");
            spType.put(8, "被动");

            skillType.put(0, "被动");
            skillType.put(1, "手动触发");
            skillType.put(2, "自动触发");

            String skillLevel = "7";
            //默认7级的技能描述
            if (messageInfo.getArgs().size() == 3) {
                skillLevel = messageInfo.getArgs().get(2);
            }

            //干员名或者技能名
            String name = messageInfo.getArgs().get(1);

            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals(""))
                name = realName;

            //把等级字符串切分（可输入范围）
            String[] levels = skillLevel.split("-");
            List<List<SkillDesc>> skillDesc = new ArrayList<>();
            //如果可以分解(既是输入的范围)
            if (levels.length == 2) {
                //先把专一专二专三转换成数字
                if (levelStr.containsKey(levels[0]))
                    levels[0] = levelStr.get(levels[0]);
                if (levelStr.containsKey(levels[1]))
                    levels[1] = levelStr.get(levels[1]);
                int min = Integer.parseInt(levels[0]);
                int max = Integer.parseInt(levels[1]);
                //取最大值最小值
                if (min > max) {
                    int a = max;
                    max = min;
                    min = a;
                }
                //遍历插入结果集
                for (int i = min; i <= max; i++) {
                    List<SkillDesc> descs = skillDescMapper.selectSkillDescByNameAndLevel(name, i);
                    if (descs.size() > 0) {
                        skillDesc.add(descs);
                    }
                }
                //不可分解(输入准确的等级)
            } else if (levels.length == 1) {
                if (levelStr.containsKey(skillLevel))
                    messageInfo.getArgs().add(2, levelStr.get(skillLevel));
                List<SkillDesc> descs = skillDescMapper.selectSkillDescByNameAndLevel(name, Integer.parseInt(skillLevel));
                if (descs.size() > 0) {
                    skillDesc.add(descs);
                }
            }

            if (skillDesc.size() == 0) {
                replayInfo.setReplayMessage("未找到相应技能描述");
            } else {
                TextLine textLine = new TextLine();
                textLine.addImage(ImageIO.read(new File(skillDesc.get(0).get(0).getAvatar())));
                String operatorName = skillDesc.get(0).get(0).getOperatorName();
                textLine.addString(operatorName);
                textLine.nextLine();
                textLine.nextLine();
                for (List<SkillDesc> list : skillDesc) {
                    for (SkillDesc sd : list) {
                        File png = new File(sd.getSkillPng());
                        if (png.exists()) {
                            textLine.addImage(ImageIO.read(png));
                        } else {
                            textLine.addSpace(3);
                        }
                        textLine.addString(sd.getSkillName() + " level" + sd.getSkillLevel() + ":");
                        textLine.nextLine();
                        textLine.addString(sd.getSpInit() + "/" + sd.getSpCost() + " 持续" + sd.getDuration() + "秒 " + spType.get(sd.getSpType()) + "/" + skillType.get(sd.getSkillType()) + (sd.getMaxCharge() == 1 ? "" : "最大充能") + sd.getMaxCharge());
                        textLine.nextLine();
                        textLine.addString(sd.getDescription());
                        textLine.nextLine();
                    }
                    textLine.nextLine();
                }
                replayInfo.setReplayImg(textLine.drawImage());
            }
        } else {
            replayInfo.setReplayMessage("请输入干员技能相关信息");
        }
        return replayInfo;
    }
}
