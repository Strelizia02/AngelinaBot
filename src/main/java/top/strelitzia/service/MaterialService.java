package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.dao.*;
import top.strelitzia.model.*;
import top.strelitzia.util.DescriptionTransformationUtil;
import top.strelitzia.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

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
    private SkillMateryMapper skillMateryMapper;

    @Autowired
    private MaterialMadeMapper materialMadeMapper;

    @Autowired
    private NickNameMapper nickNameMapper;

    @Autowired
    private EnemyMapper enemyMapper;

    @AngelinaGroup(keyWords = {"专精材料", "技能专精"}, description = "技能专精材料")
    public ReplayInfo ZhuanJingCaiLiao(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<MaterialInfo> materialInfos;
        List<String> args = messageInfo.getArgs();
        String skillName = "";
        Integer level = 0;
        if (args.size() == 4) {
            //四个参数就是##专精材料-干员—第几技能-专精等级
            Integer index = DescriptionTransformationUtil.ChangeStringToInt(args.get(2));
            level = DescriptionTransformationUtil.ChangeStringToInt(args.get(3));
            String name = nickNameMapper.selectNameByNickName(args.get(1));
            if (name != null && !name.equals("")) {
                args.add(1, name);
            }
            skillName = skillMateryMapper.selectSkillNameByAgentIndex(args.get(1), index);
            materialInfos = skillMateryMapper.selectSkillUpByAgentAndIndex(args.get(1), index, level);
        } else if (args.size() == 3) {
            //三个参数就是##专精材料-技能名-专精等级
            skillName = args.get(1);
            level = DescriptionTransformationUtil.ChangeStringToInt(args.get(2));
            materialInfos = skillMateryMapper.selectSkillUpBySkillName(args.get(1), level);
        } else {
            materialInfos = null;
        }
        if (materialInfos == null || materialInfos.size() == 0) {
            replayInfo.setReplayMessage("找不到查询的内容");
        } else {
            replayInfo.setReplayImg(sendImageWithPicAndStr(materialInfos, skillName + " 专精" + level + " 材料为："));
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"精一材料", "精二材料"}, description = "查询干员精英化材料")
    public ReplayInfo JingYingHuaCaiLiao(MessageInfo messageInfo) {
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
                replayInfo.setReplayImg(sendImageWithPicAndStr(materialInfos, agent + "干员 精英" + level + " 需要的材料为："));
            }
        } else {
            replayInfo.setReplayMessage("请输入干员名称");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"材料合成", "合成路线"}, description = "查询材料的合成途径")
    public ReplayInfo HeChengLuXian(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String name = messageInfo.getArgs().get(1);
            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals(""))
                name = realName;

            List<MaterialInfo> materialInfos = materialMadeMapper.selectMadeMater(name);
            if (materialInfos.size() == 0) {
                replayInfo.setReplayMessage("找不到该材料的合成路线");
            } else {
                replayInfo.setReplayImg(sendImageWithPicAndStr(materialInfos, name + "的合成路线为："));
            }
        } else {
            replayInfo.setReplayMessage("请输入材料名称");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"材料获取", "获取途径", "材料掉落"}, description = "查询某材料的获取地图")
    public ReplayInfo HuoQuTuJing(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String name = messageInfo.getArgs().get(1);
            StringBuilder s;
            if (!name.endsWith("-all")) {
                String realName = nickNameMapper.selectNameByNickName(name);
                if (realName != null && !realName.equals(""))
                    name = realName;
                List<SourcePlace> sourcePlaces = materialMadeMapper.selectMaterSource(name);
                s = new StringBuilder(name + "的主线关卡掉率排行前十为：");
                if (sourcePlaces.size() == 0) {
                    s = new StringBuilder("找不到该材料的获取关卡");
                } else {
                    for (SourcePlace p : sourcePlaces) {
                        String zoneName = p.getZoneName();
                        String code = p.getCode();
                        Double rate = p.getRate();
                        Double cost = p.getApCost() / rate * 100;

                        s.append("\n关卡名称：").append(zoneName).append("\t").append(code).append("\t掉落概率:").append(rate).append("%").append("\t期望理智：").append(String.format("%.2f", cost));
                    }
                }
                s.append("\n如需查看活动关卡，请在材料名后面加-all，中间无空格");
            } else {
                name = name.replace("-all", "");

                String realName = nickNameMapper.selectNameByNickName(name);
                if (realName != null && !realName.equals(""))
                    name = realName;

                List<SourcePlace> sourcePlaces = materialMadeMapper.selectMaterSourceAllStage(name);
                s = new StringBuilder(name + "的全部关卡（包含活动关卡）掉率排行前十为：");
                if (sourcePlaces.size() == 0) {
                    s = new StringBuilder("找不到该材料的获取关卡");
                } else {
                    for (SourcePlace p : sourcePlaces) {
                        String zoneName = p.getZoneName();
                        String code = p.getCode();
                        Double rate = p.getRate();
                        Double cost = p.getApCost() / rate * 100;
                        s.append("\n关卡名称：").append(zoneName).append("\t").append(code).append("\t掉落概率:").append(rate).append("%").append("\t期望理智：").append(String.format("%.2f", cost));
                    }
                }
            }
            replayInfo.setReplayMessage(s.toString());
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
    public ReplayInfo selectMaterByMap(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String MapId = messageInfo.getArgs().get(1);
            List<MapMatrixInfo> mapMatrixInfos = materialMadeMapper.selectMatrixByMap(MapId);

            if (mapMatrixInfos.size() == 0) {
                replayInfo.setReplayMessage("没有找到该地图掉落的材料");
            } else {
                int height = 50 * mapMatrixInfos.size() + 60;
                BufferedImage image = new BufferedImage(1500, height,
                        BufferedImage.TYPE_INT_BGR);//创建图片画布
                Graphics g = image.getGraphics();
                g.setColor(Color.WHITE); // 先用白色填充整张图片,也就是背景
                g.fillRect(0, 0, 1500, height);//画出矩形区域，以便于在矩形区域内写入文字
                g.setColor(Color.black);// 再换成黑色，以便于写入文字
                g.setFont(new Font("楷体", Font.PLAIN, 50));// 设置画笔字体
                g.drawString(MapId + "消耗理智" + materialMadeMapper.selectStageCost(MapId) + " 掉落的材料列表为：", 0, 50);
                for (int i = 0; i < mapMatrixInfos.size(); i++) {
                    MapMatrixInfo matrix = mapMatrixInfos.get(i);
                    String imgBase64 = materialMadeMapper.selectMaterialPicByName(matrix.getMaterialName());
                    g.drawImage(ImageUtil.Base64ToImageBuffer(imgBase64), 0, (i + 1) * 50, 50, 50, null);// 画出材料图标
                    g.drawString(matrix.getMaterialName() + "\t掉率：" + matrix.getRate() + "%\t测试次数：" + matrix.getTimes() + "\t掉落个数：" + matrix.getQuantity(), 50, (i + 2) * 50);
                }
                g.dispose();
                replayInfo.setReplayImg(image);
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

    /**
     * 材料结果转图片发送
     *
     * @param materialInfos 材料列表
     * @param s             描述文字
     */
    public BufferedImage sendImageWithPicAndStr(List<MaterialInfo> materialInfos, String s) {
        int height = 100 * materialInfos.size() + 120;
        BufferedImage image = new BufferedImage(s.length() * 100, height,
                BufferedImage.TYPE_INT_BGR);//创建图片画布
        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE); // 先用白色填充整张图片,也就是背景
        g.fillRect(0, 0, s.length() * 100, height);//画出矩形区域，以便于在矩形区域内写入文字
        g.setColor(Color.black);// 再换成黑色，以便于写入文字
        g.setFont(new Font("楷体", Font.PLAIN, 100));// 设置画笔字体
        g.drawString(s, 0, 100);
        for (int i = 0; i < materialInfos.size(); i++) {
            MaterialInfo m = materialInfos.get(i);
            String imgBase64 = materialMadeMapper.selectMaterialPicByName(m.getMaterialName());
            g.drawImage(ImageUtil.Base64ToImageBuffer(imgBase64), 0, (i + 1) * 100, 100, 100, null);// 画出材料图标
            g.drawString(m.getMaterialName() + " * " + m.getMaterialNum() + "个", 100, (i + 2) * 100);
        }
        g.dispose();
        return image;
    }
}
