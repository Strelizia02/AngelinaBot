package top.strelitzia.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.model.TextLine;
import top.angelinaBot.util.MiraiFrameUtil;
import top.angelinaBot.util.SendMessageUtil;
import top.strelitzia.dao.DailyRemindMapper;
import top.strelitzia.model.DailyRemindInfo;

import java.util.List;

/**
 * @author LWBmljx
 * @Date 2022/10/05 0:22
 **/

@Component
@Slf4j
public class DailyRemindJob{

    @Autowired
    private DailyRemindMapper dailyRemindMapper;

    @Autowired
    private SendMessageUtil sendMessageUtil;

    @Scheduled(cron = "${scheduled.remindquJob}")
    @Async
    public void remindquJob(){
        for (Long groupId: MiraiFrameUtil.messageIdMap.keySet()) {
            List<DailyRemindInfo> remindList=dailyRemindMapper.getDailyRemindByGroupId(groupId);
            if (remindList.size()>0){
                ReplayInfo replayInfo = new ReplayInfo();
                TextLine textLine = new TextLine();
                textLine.addCenterStringLine("今日提醒");
                for (DailyRemindInfo dailyRemindInfo:remindList){
		    if (dailyRemindInfo.getDayLeft()!=-1) {
                        dailyRemindInfo.setDayLeft(dailyRemindInfo.getDayLeft()-1);
                        dailyRemindMapper.updateDayLeft(dailyRemindInfo);
		    }
                    if (dailyRemindInfo.getDayLeft()==0) {
                        dailyRemindMapper.deleteDailyRemind(dailyRemindInfo.getGroupId(),dailyRemindInfo.getRemindContent(),dailyRemindInfo.getUserId());
                    }
		    if (dailyRemindInfo.getDayLeft()!=-1) {
                        textLine.addCenterStringLine(dailyRemindInfo.getRemindContent());
                        textLine.addCenterStringLine("剩余" + dailyRemindInfo.getDayLeft() + "天" + "@" + dailyRemindInfo.getUserId());
                    }
                    else{
                        textLine.addCenterStringLine(dailyRemindInfo.getRemindContent()+"@"+dailyRemindInfo.getUserId());
                    }
		}
                replayInfo.setReplayImg(textLine.drawImage());
                replayInfo.setGroupId(groupId);
                sendMessageUtil.sendGroupMsg(replayInfo);
            }
        }
    }
}
