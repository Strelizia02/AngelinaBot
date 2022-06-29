package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.model.TextLine;
import top.strelitzia.arknightsDao.EquipMapper;
import top.strelitzia.arknightsDao.MaterialMadeMapper;
import top.strelitzia.dao.NickNameMapper;
import top.strelitzia.arknightsDao.OperatorInfoMapper;
import top.strelitzia.model.EquipBuff;
import top.strelitzia.model.EquipInfo;
import top.strelitzia.model.MaterialInfo;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EquipService {

    @Autowired
    private EquipMapper equipMapper;

    @Autowired
    private MaterialMadeMapper materialMadeMapper;

    @Autowired
    private OperatorInfoMapper operatorInfoMapper;

    @Autowired
    private NickNameMapper nickNameMapper;

    @AngelinaGroup(keyWords = {"模组查询", "查询模组", "模组"}, description = "查询模组信息")
    public ReplayInfo getOperatorEquip(MessageInfo messageInfo) throws IOException {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String name = messageInfo.getArgs().get(1);
            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals(""))
                name = realName;

            List<EquipInfo> equipInfos = equipMapper.selectEquipByName(name);
            if (equipInfos.size() > 0) {
                TextLine textLine = new TextLine(100);
                textLine.addImage(ImageIO.read(new File(operatorInfoMapper.selectAvatarByName(name))));
                textLine.addString(name + "的模组信息为：");

                textLine.nextLine();
                for (EquipInfo equipInfo: equipInfos) {
                    String equipId = equipInfo.getEquipId();
                    List<String> strings = equipMapper.selectEquipMissionById(equipId);
                    textLine.nextLine();
                    textLine.addString("模组名称：" + equipInfo.getEquipName());
                    textLine.nextLine();
                    textLine.addString("解锁条件：");
                    textLine.nextLine();

                    int j = 1;
                    for (String mission : strings) {
                        textLine.addString(j + ". " + mission);
                        textLine.nextLine();
                        j++;
                    }

                    int i = equipInfo.getEquipLevel();
                    List<EquipBuff> equipBuffs = equipMapper.selectEquipBuffById(equipId, i);
                    textLine.nextLine();
                    textLine.addString("等级" + i + ":");
                    textLine.nextLine();
                    String[] desc = equipInfo.getDesc().split("\\|\\|\\|");
                    textLine.addString("模组特性：");
                    textLine.nextLine();
                    textLine.addSpace(2);
                    textLine.addString("新增天赋：");
                    textLine.nextLine();
                    textLine.addSpace(4);
                    textLine.addString(desc[0]);
                    textLine.nextLine();
                    textLine.addSpace(2);
                    textLine.addString("天赋变化：");
                    textLine.nextLine();
                    textLine.addSpace(4);
                    textLine.addString(desc[1]);
                    textLine.nextLine();
                    textLine.addString("解锁等级： 精英化" + equipInfo.getPhase() + equipInfo.getLevel() + "级");
                    textLine.nextLine();

                    textLine.addString("面板变化：");
                    textLine.nextLine();
                    for (EquipBuff e : equipBuffs) {
                        String value;
                        if (e.getValue() >= 0) {
                            value = "+" + e.getValue();
                        } else {
                            value = "-" + e.getValue();
                        }
                        textLine.addSpace(2);
                        textLine.addString(returnBuffName(e.getBuffName()));
                        textLine.addSpace();
                        textLine.addString(value);
                        textLine.nextLine();
                    }
                    textLine.addString("解锁材料：");
                    textLine.nextLine();

                    List<MaterialInfo> materialInfos = equipMapper.selectEquipCostById(equipId, i);
                    for (MaterialInfo m : materialInfos) {
                        textLine.addImage(ImageIO.read(new File(materialMadeMapper.selectMaterialPicByName(m.getMaterialName()))));
                        textLine.addString(m.getMaterialName() + " * " + m.getMaterialNum() + "个");
                        textLine.nextLine();
                    }

                }

                replayInfo.setReplayImg(textLine.drawImage());
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
        map.put("respawn_time", "再部署");
        map.put("attack_speed", "攻击速度");

        if (map.get(BuffId) != null) {
            return map.get(BuffId);
        }
        else {
            return BuffId;
        }
    }
}
