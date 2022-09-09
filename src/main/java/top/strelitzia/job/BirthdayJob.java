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
import top.strelitzia.dao.BirthdayRemindMapper;
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
    private BirthdayRemindMapper birthdayRemindMapper;

    @Autowired
    private SendMessageUtil sendMessageUtil;

    @Autowired
    private MiraiFrameUtil miraiFrameUtil;

    //每天早8点发送当日生日提醒
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

        List<String> operatorByBirthday = operatorInfoMapper.getOperatorByBirthday(today);
        if (operatorByBirthday != null && operatorByBirthday.size() > 0) {
            //今日有干员过生日,启动推送判断
            Map<Long,List<String>> birthdayMap = new HashMap<>();//群组对名字的map
            for (String name : operatorByBirthday) {//遍历每一个今天过生日的干员
                List<Long> groupList = birthdayRemindMapper.selectGroupIdByName(name);//查找加入了生日提醒的群组集合
                if (groupList.size()>0){//当集合内有群组时
                    for(Long groupId :groupList){//遍历群组集合取出每一个群组编号
                        List<String> nameList = new ArrayList<>();//干员名字的集合
                        if(birthdayMap.containsKey(groupId)) nameList = birthdayMap.get(groupId);//查找map是否已有该群组信息，如果有则取出
                        nameList.add(name);//把干员名字写入集合内
                        birthdayMap.put(groupId,nameList);//写入map
                    }
                }
            }
            for (Long groupId: MiraiFrameUtil.messageIdMap.keySet()) {
            for(Long groupId : birthdayMap.keySet()){
                StringBuilder s = new StringBuilder("今天是" + today + "祝 ");
                List<String> nameList = birthdayMap.get(groupId);
                for (String name : nameList){
                    s.append(name).append(" ");
                }
                s.append("干员生日快乐");
                ReplayInfo replayInfo = new ReplayInfo();
                replayInfo.setReplayMessage(s.toString());
                replayInfo.setGroupId(groupId);
                //只发给查询到的最新的群组列表
                if (groupList.containsKey(groupId)) {
                    replayInfo.setLoginQQ(MiraiFrameUtil.messageIdMap.get(groupId));
                    sendMessageUtil.sendGroupMsg(replayInfo);
                }
                try {
                    //线程休眠数秒再发送，减少群发频率
                    Thread.sleep(new Random().nextInt(5)*1000);
                }catch (InterruptedException e){
                    log.error(e.toString());
                }
            }
            log.info("{}每日干员生日推送发送成功", new Date());
        }
    }
}
