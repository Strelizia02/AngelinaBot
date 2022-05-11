package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.dao.NickNameMapper;
import top.strelitzia.arknightsDao.OperatorInfoMapper;
import top.strelitzia.model.OperatorBasicInfo;
import top.strelitzia.model.TalentInfo;

import java.util.List;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
@Service
public class OperatorInfoService {

    @Autowired
    private OperatorInfoMapper operatorInfoMapper;

    @Autowired
    private NickNameMapper nickNameMapper;


    @AngelinaGroup(keyWords = {"干员查询", "查询干员"}, description = "根据条件查询干员")
    public ReplayInfo getOperatorByInfos(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<String> infos = messageInfo.getArgs();
        List<String> operators = operatorInfoMapper.getAllOperator();
        StringBuilder s = new StringBuilder("符合 ");
        for (int i = 1; i < infos.size(); i++) {
            String info = infos.get(i);
            if (info == null) {
                break;
            }

            String realName = nickNameMapper.selectNameByNickName(info);
            if (realName != null && !realName.equals(""))
                info = realName;

            List<String> operatorNameByInfo = operatorInfoMapper.getOperatorNameByInfo(info);
            operators.retainAll(operatorNameByInfo);
            s.append(info).append(" ");
        }
        s.append("条件的干员为：\n");
        for (String name : operators) {
            s.append(name).append("\n");
        }
        if (infos.contains("叶莲娜")||infos.contains("霜星")) {
            s.append("霜星").append("\n");
        }
        replayInfo.setReplayMessage(s.toString());
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"档案信息", "查询档案", "干员档案", "档案查询"}, description = "查询干员档案信息")
    public ReplayInfo getOperatorInfo(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String name = messageInfo.getArgs().get(1);
            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals(""))
                name = realName;

            OperatorBasicInfo operatorInfoByName = operatorInfoMapper.getOperatorInfoByName(name);
            String s = name + "干员的档案为：\n";
            if (messageInfo.getArgs().size() == 2) {
                s += "基础档案：\n" +
                        "画师：" + operatorInfoByName.getDrawName() + '\t' +
                        "声优：" + operatorInfoByName.getInfoName() + '\n' +
                        "代号：" + operatorInfoByName.getCodeName() + '\t' +
                        "性别：" + operatorInfoByName.getSex() + '\t' +
                        "出身地：" + operatorInfoByName.getComeFrom() + '\n' +
                        "生日：" + operatorInfoByName.getBirthday() + '\t' +
                        "种族：" + operatorInfoByName.getRace() + '\t' +
                        "身高：" + operatorInfoByName.getHeight() + '\n' +
                        "矿石病感染情况：" + operatorInfoByName.getInfection();
            } else {
                switch (messageInfo.getArgs().get(2)) {
                    case "全部档案":
                        s += operatorInfoByName.toString();
                        break;
                    case "基础档案":
                        s += "基础档案：\n" +
                                "画师：" + operatorInfoByName.getDrawName() + '\t' +
                                "声优：" + operatorInfoByName.getInfoName() + '\n' +
                                "代号：" + operatorInfoByName.getCodeName() + '\t' +
                                "性别：" + operatorInfoByName.getSex() + '\t' +
                                "出身地：" + operatorInfoByName.getComeFrom() + '\n' +
                                "生日：" + operatorInfoByName.getBirthday() + '\t' +
                                "种族：" + operatorInfoByName.getRace() + '\t' +
                                "身高：" + operatorInfoByName.getHeight() + '\n' +
                                "矿石病感染情况：" + operatorInfoByName.getInfection();
                        break;
                    case "综合体检测试":
                        s += operatorInfoByName.getComprehensiveTest();
                        break;
                    case "客观履历":
                        s += operatorInfoByName.getObjectiveResume();
                        break;
                    case "临床诊断分析":
                        s += operatorInfoByName.getClinicalDiagnosis();
                        break;
                    case "档案资料一":
                        s += operatorInfoByName.getArchives1();
                        break;
                    case "档案资料二":
                        s += operatorInfoByName.getArchives2();
                        break;
                    case "档案资料三":
                        s += operatorInfoByName.getArchives3();
                        break;
                    case "档案资料四":
                        s += operatorInfoByName.getArchives4();
                        break;
                    case "晋升记录":
                    case "晋升资料":
                        s += operatorInfoByName.getPromotionInfo();
                        break;
                }
            }
            replayInfo.setReplayMessage(s);
        } else {
            replayInfo.setReplayMessage("请输入干员名称");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"声优查询", "查询声优"}, description = "根据条件查询声优信息")
    public ReplayInfo getCVByName(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<String> allCV;
        StringBuilder s = new StringBuilder();
        if (messageInfo.getArgs().size() > 1) {
            allCV = operatorInfoMapper.getAllInfoNameLikeStr(messageInfo.getArgs().get(1));
        } else {
            allCV = operatorInfoMapper.getAllInfoName();
        }
        for (String name : allCV) {
            s.append(name).append('\n');
        }
        replayInfo.setReplayMessage(s.toString());
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"画师查询", "查询画师"}, description = "根据条件查询画师信息")
    public ReplayInfo getDrawByName(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<String> allDraw;
        StringBuilder s = new StringBuilder();
        if (messageInfo.getArgs().size() > 1) {
            allDraw = operatorInfoMapper.getAllDrawNameLikeStr(messageInfo.getArgs().get(1));
        } else {
            allDraw = operatorInfoMapper.getAllDrawName();
        }
        for (String name : allDraw) {
            s.append(name).append('\n');
        }
        replayInfo.setReplayMessage(s.toString());
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"天赋查询", "干员天赋"}, description = "查询干员的天赋信息")
    public ReplayInfo getTalentByName(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String name = messageInfo.getArgs().get(1);
            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals(""))
                name = realName;

            List<TalentInfo> operatorTalent = operatorInfoMapper.getOperatorTalent(name);
            if (operatorTalent != null && operatorTalent.size() > 0) {
                StringBuilder s = new StringBuilder(name).append("干员的天赋为：");
                for (TalentInfo t : operatorTalent) {
                    s.append("\n").append(t.getTalentName()).append("\t解锁条件：精英化").append(t.getPhase()).append("等级")
                            .append(t.getLevel()).append("潜能").append(t.getPotential())
                            .append("\n\t").append(t.getDescription());
                }
                replayInfo.setReplayMessage(s.toString());
            } else {
                replayInfo.setReplayMessage("未找到该干员的天赋");
            }
        } else {
            replayInfo.setReplayMessage("请输入干员名称");
        }
        return replayInfo;
    }
}
