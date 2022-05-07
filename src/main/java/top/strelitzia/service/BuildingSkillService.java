package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.arknightsDao.BuildingSkillMapper;
import top.strelitzia.dao.NickNameMapper;
import top.strelitzia.arknightsDao.OperatorInfoMapper;
import top.strelitzia.model.BuildingSkill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
@Service
public class BuildingSkillService {

    @Autowired
    private BuildingSkillMapper buildingSkillMapper;

    @Autowired
    private OperatorInfoMapper operatorInfoMapper;

    @Autowired
    private NickNameMapper nickNameMapper;


    @AngelinaGroup(keyWords = {"基建技能"}, description = "查询基建技能信息")
    public ReplayInfo getBuildSkillNameServiceByInfos(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);

        Map<String, String> roomTypeMap = new HashMap<>();
        roomTypeMap.put("控制中枢", "CONTROL");
        roomTypeMap.put("宿舍", "DORMITORY");
        roomTypeMap.put("办公室", "HIRE");
        roomTypeMap.put("制造站", "MANUFACTURE");
        roomTypeMap.put("会客室", "MEETING");
        roomTypeMap.put("发电站", "POWER");
        roomTypeMap.put("贸易站", "TRADING");
        roomTypeMap.put("训练室", "TRAINING");
        roomTypeMap.put("加工站", "WORKSHOP");

        List<BuildingSkill> allBuildingSkill = buildingSkillMapper.getAllBuildingSkill();
        for (int i = 1; i < messageInfo.getArgs().size(); i++) {
            String info = messageInfo.getArgs().get(i);
            if (info == null) {
                break;
            }
            if (roomTypeMap.containsKey(info)) {
                info = roomTypeMap.get(info);
            }

            String name = nickNameMapper.selectNameByNickName(info);
            if (name != null && !name.equals(""))
                info = name;

            List<BuildingSkill> buildingSkillByInfo = buildingSkillMapper.getBuildingSkillByInfo(info);
            allBuildingSkill.retainAll(buildingSkillByInfo);
        }
        StringBuilder s = new StringBuilder("查询到的基建技能为：\n");
        if (allBuildingSkill.size() == 0) {
            return replayInfo;
        } else if (allBuildingSkill.size() >= 4) {
            for (BuildingSkill b : allBuildingSkill) {
                s.append(b.getBuffName()).append("\n");
            }
            s.append("结果过多，只显示对应基建技能名称。\n如需查看基建技能详细信息，请缩小搜索范围，比如使用技能名或者干员名来查询");
        } else {
            for (BuildingSkill b : allBuildingSkill) {
                String name = operatorInfoMapper.getOperatorNameById(b.getOperatorId());
                s.append(name).append(" ").append(b.getBuffName()).append(" 精英").append(b.getPhase()).append("/").append(b.getLevel()).append("级解锁\n\t").append(b.getDescription()).append("\n");
            }
        }
        replayInfo.setReplayMessage(s.toString());
        return replayInfo;
    }

}
