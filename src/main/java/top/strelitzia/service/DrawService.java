package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.model.TextLine;
import top.strelitzia.dao.DailyRemindMapper;
import top.strelitzia.model.BiliCount;
import top.strelitzia.model.DailyRemindInfo;
import top.strelitzia.model.DailyRemindInfo;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DrawService{

    @Autowired
    private DailyRemindMapper dailyRemindMapper;

    @AngelinaGroup(keyWords = {"决定","抽数","骰子"}, description = "让龟龟帮你做决定！龟龟决定后接一个数字抽数（默认为1d6）")
    public ReplayInfo RandomService(MessageInfo messageInfo){
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        int randomMax ;
        int randomTimes;
        //判断数字
        if (messageInfo.getArgs().size()>1){
            if (messageInfo.getArgs().get(1).matches("\\d+"))
            {
                randomMax = Integer.parseInt(messageInfo.getArgs().get(1));
                randomTimes=1;
            }
            else if (messageInfo.getArgs().get(1).matches("([0-9]+)d([0-9]+)"))
            {
                Pattern p=Pattern.compile("(\\d+)d(\\d+)");
                Matcher m=p.matcher(messageInfo.getArgs().get(1));
                m.find();
                randomTimes=Integer.parseInt(m.group(1));
                randomMax=Integer.parseInt(m.group(2));
            }
            else
            {
                randomMax = 6;
                randomTimes = 1 ; 
            }
        }else {
            randomMax = 6;
            randomTimes = 1 ;
        }
        Random random = new Random(System.nanoTime() + System.currentTimeMillis() / messageInfo.getQq());
        int total=random.nextInt(randomMax)+1;
        String replayin=messageInfo.getName()+"，您的抽取结果是"+total;
        int tempRandom;
        if (randomTimes>1)
        {
            for (int i =1;i<randomTimes;i=i+1)
            {
                tempRandom=random.nextInt(randomMax)+1;
                total+=tempRandom;
                replayin=replayin.concat("+"+tempRandom);
            }
            replayin=replayin.concat("="+total);
        }
        replayInfo.setReplayMessage(replayin);
        return replayInfo;
        }

    @AngelinaGroup(keyWords = {"提醒"}, description = "设置一个提醒，会在每天中午12点提醒你")
    public ReplayInfo setDailyRemind(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            DailyRemindInfo dailyRemindInfo=new DailyRemindInfo();
	    dailyRemindInfo.setGroupId(messageInfo.getGroupId());
            dailyRemindInfo.setRemindContent(messageInfo.getArgs().get(1));
            dailyRemindInfo.setUserId(messageInfo.getQq());
            dailyRemindInfo.setDayLeft(-1);
            Long groupId =messageInfo.getGroupId();
            if (messageInfo.getArgs().size()>2){
                boolean result = messageInfo.getArgs().get(2).matches("[0-9]+");
                if (!result){
                    StringBuilder s = new StringBuilder();
                    char[] arr=messageInfo.getArgs().get(2).toCharArray();
                    for(char c :arr){
                        if (c>=48&&c<=57){
                            s.append(c - '0');
                        }
                    }
                    if (s.toString().equals("")){
                        replayInfo.setReplayMessage("对不起啊博士，没能理解您的意思，请务必告诉我数字呢");
                        return replayInfo;
                    }
                    dailyRemindInfo.setDayLeft(Integer.parseInt(s.toString()));
                }else {
                    dailyRemindInfo.setDayLeft(Integer.parseInt(messageInfo.getArgs().get(2)));
                }
            }
            dailyRemindMapper.insertDailyRemind(dailyRemindInfo);
            replayInfo.setReplayMessage("设置成功");
            return replayInfo;
        }
        replayInfo.setReplayMessage("你没有输入提醒！");
        return replayInfo;
    }
    @AngelinaGroup(keyWords = {"关闭提醒","取消提醒","删除提醒"}, description = "取消一个已经设定的提醒")
    public ReplayInfo removeDailyRemind(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String remindContent = messageInfo.getArgs().get(1);
            Long groupId = messageInfo.getGroupId();
            Long userId=messageInfo.getQq();
            dailyRemindMapper.deleteDailyRemind(groupId, remindContent,userId);
            replayInfo.setReplayMessage("取消成功（注意：我不会检查你的输入是否正确）");
            return  replayInfo;
        }
        replayInfo.setReplayMessage("你没有输入要取消的提醒！");
        return replayInfo;
    }
    @AngelinaGroup(keyWords = {"提醒列表"}, description = "查看本群的所有提醒")
    public ReplayInfo getDailyRemindList(MessageInfo messageInfo) {
        List<DailyRemindInfo> remindList = dailyRemindMapper.getDailyRemindByGroupId(messageInfo.getGroupId());
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        TextLine textLine = new TextLine();
        if (remindList.size() > 0) {
            textLine.addCenterStringLine("提醒列表");
            for (DailyRemindInfo dailyRemindInfo : remindList) {
		if (dailyRemindInfo.getDayLeft()!=-1) {
                        textLine.addCenterStringLine(dailyRemindInfo.getRemindContent());
                        textLine.addCenterStringLine("剩余" + dailyRemindInfo.getDayLeft() + "天" + "@" + dailyRemindInfo.getUserId());
                    }
                    else{
                        textLine.addCenterStringLine(dailyRemindInfo.getRemindContent()+"@"+dailyRemindInfo.getUserId());
                    }
            }
            replayInfo.setReplayImg(textLine.drawImage());
        }else {
            replayInfo.setReplayMessage("本群暂时还没有提醒内容哦~");
        }
        return replayInfo;
    }
}
