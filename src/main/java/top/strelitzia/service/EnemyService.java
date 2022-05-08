package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.arknightsDao.EnemyMapper;
import top.strelitzia.model.EnemyInfo;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
@Service
public class EnemyService {

    @Autowired
    private EnemyMapper enemyMapper;

    @AngelinaGroup(keyWords = {"敌人查询", "查询敌人", "敌人面板"}, description = "查询敌人面板")
    public ReplayInfo getEnemyInfoByName(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String name = messageInfo.getArgs().get(1);
            List<EnemyInfo> enemyInfo = enemyMapper.selectEnemyByName(name);
            StringBuilder s = new StringBuilder();
            if (name.contains("霜星")) {
                s.append("霜星是我们罗德岛的干员哦。\n");
            }

            if (enemyInfo.size() == 0) {
                s = new StringBuilder("未找到该敌人的信息");
            } else {
                for (EnemyInfo info : enemyInfo) {
                    s.append("\n").append(info.toString());
                }
            }
            replayInfo.setReplayMessage(s.toString());
        } else {
            replayInfo.setReplayMessage("请输入敌人的信息");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"敌人名字", "敌人全名"}, description = "查询敌人的全名")
    public ReplayInfo getEnemyListByName(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String name = messageInfo.getArgs().get(1);
            List<String> nameList = enemyMapper.selectEnemyListByName(name);
            Set<String> names = new TreeSet<>(nameList);
            StringBuilder s = new StringBuilder("搜索到的敌人名称为：");
            for (String enemyName : names) {
                s.append("\n").append(enemyName);
            }
            replayInfo.setReplayMessage(s.toString());
        } else {
            replayInfo.setReplayMessage("请输入敌人的部分名称");
        }
        return replayInfo;
    }
}
