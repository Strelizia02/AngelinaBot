package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.FunctionType;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.PermissionEnum;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.util.MiraiFrameUtil;
import top.angelinaBot.util.SendMessageUtil;
import top.strelitzia.dao.ExecuteSqlMapper;

import java.util.List;

/**
 * @author strelitzia
 * @Date 2022/05/03 14:38
 **/
@Service
public class ExecuteSqlService {

    @Autowired
    private ExecuteSqlMapper executeSqlMapper;

    @Autowired
    private SendMessageUtil sendMessageUtil;

    @AngelinaGroup(keyWords = {"sql", "SQL"}, funcClass = FunctionType.FunctionAdmin, permission = PermissionEnum.Administrator)
    public ReplayInfo ExecuteSql(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            List<String> text = messageInfo.getArgs();
            StringBuilder sql = new StringBuilder();
            for (int i= 1; i < text.size(); i++) {
                 sql.append(" ").append(text.get(i));
            }
            String s = executeSqlMapper.executeSql(sql.toString()).toString();
            replayInfo.setReplayMessage(s);
        } else {
            replayInfo.setReplayMessage("请输入sql语句");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"群发消息"}, funcClass = FunctionType.FunctionAdmin, permission = PermissionEnum.Administrator)
    public ReplayInfo sendGroupMessage(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo();
        if (messageInfo.getArgs().size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < messageInfo.getArgs().size(); i++) {
                sb.append(messageInfo.getArgs().get(i));
            }
            replayInfo.setReplayMessage(sb.toString());
        }
        for (String url: messageInfo.getImgUrlList()) {
            replayInfo.setReplayImg(url);
        }

        replayInfo.setGroupId(MiraiFrameUtil.messageIdMap.keySet());
        sendMessageUtil.sendGroupMsg(replayInfo);
        return null;
    }
}
