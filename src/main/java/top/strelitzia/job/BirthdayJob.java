package top.strelitzia.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.util.MiraiFrameUtil;
import top.angelinaBot.util.SendMessageUtil;
import top.strelitzia.arknightsDao.OperatorInfoMapper;
import top.strelitzia.dao.UserFoundMapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author wangzy
 * @Date 2020/12/16 14:10
 **/
@Component
@Slf4j
public class BirthdayJob {
    @Autowired
    private OperatorInfoMapper operatorInfoMapper;

    @Autowired
    private UserFoundMapper userFoundMapper;

    @Autowired
    private SendMessageUtil sendMsgUtil;

    //每天晚上8点发送当日统计结果
    @Scheduled(cron = "${scheduled.birthdayJob}")
    @Async
    public void birthdayJob() {
        SimpleDateFormat month = new SimpleDateFormat("MM");
        SimpleDateFormat day = new SimpleDateFormat("dd");
        Date date = new Date();

        String monthStr = month.format(date);
        String dayStr = day.format(date);
        if (monthStr.startsWith("0")) {
            monthStr = monthStr.substring(1);
        }
        if (dayStr.startsWith("0")) {
            dayStr = dayStr.substring(1);
        }
        String today = monthStr + "月" + dayStr + "日";

        StringBuilder s = new StringBuilder("今天是" + today + "祝 ");
        List<String> operatorByBirthday = operatorInfoMapper.getOperatorByBirthday(today);
        if (operatorByBirthday != null && operatorByBirthday.size() > 0) {
            //今日有干员过生日才推送
            for (String name : operatorByBirthday) {
                s.append(name).append(" ");
            }
            s.append("干员生日快乐");
            ReplayInfo replayInfo = new ReplayInfo();
            replayInfo.setReplayMessage(s.toString());
            for (Long groupId: MiraiFrameUtil.messageIdMap.keySet()) {
                replayInfo.setGroupId(groupId);
                replayInfo.setLoginQQ(MiraiFrameUtil.messageIdMap.get(groupId));
                sendMsgUtil.sendGroupMsg(replayInfo);
            }
            log.info("{}每日干员生日推送发送成功", new Date());
        }
    }
}
