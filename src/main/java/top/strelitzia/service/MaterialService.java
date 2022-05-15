package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.model.TextLine;
import top.strelitzia.arknightsDao.*;
import top.strelitzia.dao.*;
import top.strelitzia.model.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
@Service
@Slf4j
public class MaterialService {

    @Autowired
    private OperatorEvolveMapper operatorEvolveMapper;

    @Autowired
    private OperatorInfoMapper operatorInfoMapper;

    @Autowired
    private SkillMateryMapper skillMateryMapper;

    @Autowired
    private MaterialMadeMapper materialMadeMapper;

    @Autowired
    private NickNameMapper nickNameMapper;

    @Autowired
    private EnemyMapper enemyMapper;

    @AngelinaGroup(keyWords = {"专精材料", "技能专精", "专精"}, description = "技能专精材料")
    public ReplayInfo ZhuanJingCaiLiao(MessageInfo messageInfo) throws IOException {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            Integer level = 3;
            if (messageInfo.getArgs().size() > 2) {
                Map<String, Integer> map = new HashMap<>(10);
                map.put("专精一", 1);
                map.put("专精二", 2);
                map.put("专一", 1);
                map.put("专二", 2);
                map.put("精一", 1);
                map.put("精二", 2);
                map.put("一", 1);
                map.put("二", 2);
                map.put("1", 1);
                map.put("2", 2);
                if (map.containsKey(messageInfo.getArgs().get(2))) {
                    level = map.get(messageInfo.getArgs().get(2));
                }
            }
            String name = messageInfo.getArgs().get(1);
            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals("")) {
                name = realName;
            }
            List<Integer> skillIds = skillMateryMapper.selectSkillIDByAgentOrSkill(name);
            if (skillIds == null || skillIds.size() == 0) {
                replayInfo.setReplayMessage("找不到查询的内容");
            } else {
                TextLine textLine = new TextLine();
                OperatorName operatorName = skillMateryMapper.selectOperatorNameById(skillIds.get(0));
                String avatar = operatorInfoMapper.selectOperatorAvatarPngById(operatorName.getCharId());
                if (avatar != null) {
                    File avatarFile = new File(avatar);
                    if (avatarFile.exists()) {
                        textLine.addImage(ImageIO.read(avatarFile));
                    } else {
                        textLine.addSpace(3);
                    }
                }else {
                    textLine.addSpace(3);
                }
                textLine.addString("干员" + operatorName.getOperatorName() + "的技能专精材料为：");
                textLine.nextLine();
                for (Integer id : skillIds) {
                    List<MaterialInfo> materialInfos = skillMateryMapper.selectSkillUpByIdAndLevel(id, level);
                    String pathname = skillMateryMapper.selectSkillPngByName(id);

                    if (pathname != null) {
                        File skillPng = new File(pathname);
                        if (skillPng.exists()) {
                            textLine.addImage(ImageIO.read(skillPng));
                        } else {
                            textLine.addSpace(3);
                        }
                    }else {
                        textLine.addSpace(3);
                    }
                    textLine.addString(skillMateryMapper.selectSkillNameById(id) + " 专精" + level + "：");
                    textLine.nextLine();
                    for (MaterialInfo m : materialInfos) {
                        textLine.addSpace(2);
                        File file = new File(materialMadeMapper.selectMaterialPicByName(m.getMaterialName()));
                        if (file.exists()) {
                            textLine.addImage(ImageIO.read(file));
                        } else {
                            textLine.addSpace(3);
                        }
                        textLine.addString(m.getMaterialName() + " * " + m.getMaterialNum() + "个");
                        textLine.nextLine();
                    }
                }
                replayInfo.setReplayImg(textLine.drawImage());
            }
        } else {
            replayInfo.setReplayMessage("请输入需要查询的技能名或干员名");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"精一材料", "精二材料"}, description = "查询干员精英化材料")
    public ReplayInfo JingYingHuaCaiLiao(MessageInfo messageInfo) throws IOException {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String agent = messageInfo.getArgs().get(1);
            int level = 1;
            if (messageInfo.getKeyword().contains("精二")) {
                level = 2;
            }
            String name = nickNameMapper.selectNameByNickName(agent);
            if (name != null && !name.equals("")) {
                agent = name;
            }

            List<MaterialInfo> materialInfos = operatorEvolveMapper.selectOperatorEvolveByName(agent, level);
            if (materialInfos.size() == 0) {
                replayInfo.setReplayMessage("找不到查询的材料");
            } else {
                TextLine textLine = new TextLine();
                File avatar = new File(operatorInfoMapper.selectAvatarByName(agent));
                if (avatar.exists()) {
                    textLine.addImage(ImageIO.read(avatar));
                } else {
                    textLine.addSpace(3);
                }
                textLine.addString(agent + "精英" + level + "所需材料为：");
                textLine.nextLine();
                for (MaterialInfo m : materialInfos) {
                    textLine.addSpace(2);
                    File file = new File(materialMadeMapper.selectMaterialPicByName(m.getMaterialName()));
                    if (file.exists()) {
                        textLine.addImage(ImageIO.read(file));
                    } else {
                        textLine.addSpace(3);
                    }
                    textLine.addString(m.getMaterialName() + " * " + m.getMaterialNum() + "个");
                    textLine.nextLine();
                }
                replayInfo.setReplayImg(textLine.drawImage());
            }
        } else {
            replayInfo.setReplayMessage("请输入干员名称");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"材料合成", "合成路线"}, description = "查询材料的合成途径")
    public ReplayInfo HeChengLuXian(MessageInfo messageInfo) throws IOException {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String name = messageInfo.getArgs().get(1).toUpperCase();
            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals(""))
                name = realName;

            List<MaterialInfo> materialInfos = materialMadeMapper.selectMadeMater(name);
            if (materialInfos.size() == 0) {
                replayInfo.setReplayMessage("找不到该材料的合成路线");
            } else {
                TextLine textLine = new TextLine();
                File materialPic = new File(materialMadeMapper.selectMaterialPicByName(name));
                if (materialPic.exists()) {
                    textLine.addImage(ImageIO.read(materialPic));
                } else {
                    textLine.addSpace(3);
                }
                textLine.addString(name + "的合成路线为：");
                textLine.nextLine();
                for (MaterialInfo m : materialInfos) {
                    textLine.addSpace(2);
                    File file = new File(materialMadeMapper.selectMaterialPicByName(m.getMaterialName()));
                    if (file.exists()) {
                        textLine.addImage(ImageIO.read(file));
                    } else {
                        textLine.addSpace(3);
                    }
                    textLine.addString(m.getMaterialName() + " * " + m.getMaterialNum() + "个");
                    textLine.nextLine();
                }
                replayInfo.setReplayImg(textLine.drawImage());
            }
        } else {
            replayInfo.setReplayMessage("请输入材料名称");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"材料获取", "获取途径", "材料掉落"}, description = "查询某材料的获取地图")
    public ReplayInfo HuoQuTuJing(MessageInfo messageInfo) throws IOException {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String name = messageInfo.getArgs().get(1).toUpperCase();
            TextLine textLine = new TextLine();
            List<SourcePlace> sourcePlaces;
            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals("")) {
                name = realName;
            }
            if (!name.endsWith("-ALL")) {
                File file = new File(materialMadeMapper.selectMaterialPicByName(name));
                if (file.exists()) {
                    textLine.addImage(ImageIO.read(file));
                } else {
                    textLine.addSpace(3);
                }
                sourcePlaces = materialMadeMapper.selectMaterSource(name);
                textLine.addString(name + "的主线关卡掉率前十：");
                textLine.nextLine();
                textLine.addString("如需查看活动关卡");
                textLine.nextLine();
                textLine.addSpace(2);
                textLine.addString("请在材料名后面加-all，中间无空格");
            } else {
                name = name.replace("-ALL", "").replace("-all", "");
                File file = new File(materialMadeMapper.selectMaterialPicByName(name));
                if (file.exists()) {
                    textLine.addImage(ImageIO.read(file));
                } else {
                    textLine.addSpace(3);
                }
                sourcePlaces = materialMadeMapper.selectMaterSourceAllStage(name);
                textLine.addString(name + "的全部关卡（包含活动关卡）掉率前十：");
            }
            textLine.nextLine();
            if (sourcePlaces.size() == 0) {
                replayInfo.setReplayMessage("找不到该材料的获取关卡");
            } else {
                for (SourcePlace p : sourcePlaces) {
                    String zoneName = p.getZoneName();
                    String code = p.getCode();
                    Double rate = p.getRate();
                    Double cost = p.getApCost() / rate * 100;
                    textLine.addString("关卡名称：");
                    textLine.addString(zoneName);
                    textLine.addSpace();
                    textLine.addString(code);
                    textLine.nextLine();
                    textLine.addSpace();
                    textLine.addString("掉落概率:");
                    textLine.addString(String.format("%.2f", rate));
                    textLine.addString("%");
                    textLine.nextLine();
                    textLine.addSpace();
                    textLine.addString("期望理智：");
                    textLine.addString(String.format("%.2f", cost));
                    textLine.nextLine();
                }
                replayInfo.setReplayImg(textLine.drawImage());
            }
        } else {
            replayInfo.setReplayMessage("请输入材料名称");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"干员面板", "面板"}, description = "查询干员面板")
    public ReplayInfo selectAgentData(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String name = messageInfo.getArgs().get(1);
            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals("")) {
                name = realName;
            }
            if (name.contains("霜星")) {
                List<EnemyInfo> enemyInfo = enemyMapper.selectEnemyByName(name);
                StringBuilder s = new StringBuilder();
                for (EnemyInfo info : enemyInfo) {
                    s.append("\n").append(info.toString());
                }
                replayInfo.setReplayMessage(s.toString());
            } else {
                OperatorData operatorData = operatorEvolveMapper.selectOperatorData(name);
                String s = "未找到该干员数据";
                if (operatorData.getAtk() != null) {
                    s = name + "满精英化满级，无信赖无潜能面板为：" +
                            "\n生命上限：" + operatorData.getMaxHp() + "\t攻击：" + operatorData.getAtk() +
                            "\n防御：" + operatorData.getDef() + "\t法术抵抗：" + operatorData.getMagicResistance() +
                            "\n部署费用：" + operatorData.getCost() + "\t阻挡数：" + operatorData.getBlockCnt() +
                            "\n攻击间隔：" + operatorData.getBaseAttackTime() + "s\t再部署：" + operatorData.getRespawnTime() + "s";
                }
                replayInfo.setReplayMessage(s);
            }
        } else {
            replayInfo.setReplayMessage("请输入干员名");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"地图掉落", "掉落查询"}, description = "查询某地图掉落材料的概率")
    public ReplayInfo selectMaterByMap(MessageInfo messageInfo) throws IOException {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String MapId = messageInfo.getArgs().get(1).toUpperCase();
            List<MapMatrixInfo> mapMatrixInfos = materialMadeMapper.selectMatrixByMap(MapId);

            if (mapMatrixInfos.size() == 0) {
                replayInfo.setReplayMessage("没有找到该地图掉落的材料");
            } else {
                TextLine textLine = new TextLine();
                textLine.addString(MapId + "消耗理智" + materialMadeMapper.selectStageCost(MapId) + " 掉落的材料列表为：");
                textLine.nextLine();
                for (MapMatrixInfo matrix : mapMatrixInfos) {
                    File file = new File(materialMadeMapper.selectMaterialPicByName(matrix.getMaterialName()));
                    if (file.exists()) {
                        textLine.addImage(ImageIO.read(file));
                    } else {
                        textLine.addSpace(3);
                    }
                    textLine.addString((matrix.getMaterialName() + "掉率：" + String.format("%.2f", matrix.getRate()) + "%"));
                    textLine.nextLine();
                    textLine.addString("测试次数：" + matrix.getTimes());
                    textLine.nextLine();
                    textLine.addString("掉落个数：" + matrix.getQuantity());
                    textLine.nextLine();
                }
                replayInfo.setReplayImg(textLine.drawImage());
            }
        } else {
            replayInfo.setReplayMessage("请输入地图id");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"地图列表"}, description = "查询某章节的所有地图")
    public ReplayInfo selectMapList(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String zoneName = messageInfo.getArgs().get(1);
            List<MapCostInfo> mapCostInfos = materialMadeMapper.selectMapByZone(zoneName);
            StringBuilder s = new StringBuilder("地图ID以及理智花费为：");

            for (MapCostInfo mapInfo : mapCostInfos) {
                s.append("\n").append(mapInfo.getZoneName()).append("\t地图ID：").append(mapInfo.getCode()).append("\t理智消耗：").append(mapInfo.getApCost());
            }
            replayInfo.setReplayMessage(s.toString());
        } else {
            replayInfo.setReplayMessage("请输入章节名");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"章节列表"}, description = "查询所有章节")
    public ReplayInfo selectZoneList(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<String> zones = materialMadeMapper.selectAllZone();
        StringBuilder s = new StringBuilder("当前所有章节列表：");
        for (String zone : zones) {
            s.append("\n").append(zone);
        }
        replayInfo.setReplayMessage(s.toString());
        return replayInfo;
    }
}
