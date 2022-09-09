package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.arknightsDao.OperatorInfoMapper;
import top.strelitzia.dao.BirthdayRemindMapper;
import top.strelitzia.dao.NickNameMapper;

import java.util.List;

@Service
public class RemindService {

    @Autowired
    private OperatorInfoMapper operatorInfoMapper;

    @Autowired
    private BirthdayRemindMapper birthdayRemindMapper;

    @Autowired
    private NickNameMapper nickNameMapper;

    @AngelinaGroup(keyWords = {"生日提醒"}, description = "给当前群组增加一位指定干员的生日提醒")
    public ReplayInfo subscribeBirthday(MessageInfo messageInfo){
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getUserAdmin().getLevel()<1){
            replayInfo.setReplayMessage("您的权限不足");
            return replayInfo;
        }
        if (messageInfo.getArgs().size()>1){
            List<String> AllOperator = operatorInfoMapper.getAllOperator();
            String name = messageInfo.getArgs().get(1);
            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals("")) {
                name = realName;
            }
            if (AllOperator.contains(name)){
                List<String> nameByGroupId = birthdayRemindMapper.selectNameByGroupId(messageInfo.getGroupId());
                if (nameByGroupId != null && nameByGroupId.contains(name)){
                    replayInfo.setReplayMessage("博士，您已经为这位干员设有生日提醒了");
                }else {
                    birthdayRemindMapper.insertBirthdayRemind(messageInfo.getGroupId(),name);
                    replayInfo.setReplayMessage( name + "生日提醒添加成功！");
                }
            }else {
                replayInfo.setReplayMessage("这个名字我查不到呢，再检查一下吧");
            }
        }else {
            replayInfo.setReplayMessage("博士，请告诉我您想要订阅的干员名字呢");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"不再提醒"},description = "删除当前群组对指定干员的生日提醒")
    public ReplayInfo deleteSubscription(MessageInfo messageInfo){
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getUserAdmin().getLevel()<1){
            replayInfo.setReplayMessage("您的权限不足");
            return replayInfo;
        }
        if (messageInfo.getArgs().size()>1){
            List<String> AllOperator = operatorInfoMapper.getAllOperator();
            String name = messageInfo.getArgs().get(1);
            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals("")) {
                name = realName;
            }
            if (AllOperator.contains(name)){
                List<String> nameByGroupId = birthdayRemindMapper.selectNameByGroupId(messageInfo.getGroupId());
                if (nameByGroupId != null && nameByGroupId.contains(name)){
                    birthdayRemindMapper.deleteBirthdayRemind(messageInfo.getGroupId(),name);
                    replayInfo.setReplayMessage( name + "生日提醒取消成功");
                }else {
                    replayInfo.setReplayMessage("博士，您还没有为这位干员设置生日提醒呢");
                }
            }else {
                replayInfo.setReplayMessage("这个名字我查不到呢，再检查一下吧");
            }
        }else {
            replayInfo.setReplayMessage("博士，请告诉我您想要订阅的干员名字呢");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"生日提醒列表"},description = "查看群组内已添加的生日列表")
    public ReplayInfo listOfBirthday(MessageInfo messageInfo){
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<String> nameByGroupId = birthdayRemindMapper.selectNameByGroupId(messageInfo.getGroupId());
        StringBuilder s = new StringBuilder().append("当前群组已添加的生日提醒干员有：\n");
        int i = 0;
        for(String name : nameByGroupId){
            s.append(name);
            i++;
            if (i==5){
                s.append("\n");
                i=0;
            }else s.append("   ");
        }
        replayInfo.setReplayMessage(s.toString());
        return replayInfo;
    }
}
