package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.util.MiraiFrameUtil;
import top.angelinaBot.util.SendMessageUtil;
import top.strelitzia.dao.AdminUserMapper;
import top.strelitzia.dao.ExecuteSqlMapper;
import top.strelitzia.dao.UserFoundMapper;
import top.strelitzia.model.AdminUserInfo;
import top.strelitzia.util.AdminUtil;

import java.util.List;

/**
 * @author wangzy
 * @Date 2021/2/20 11:02
 **/
@Service
public class ExecuteSqlService {

    @Autowired
    private ExecuteSqlMapper executeSqlMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private UserFoundMapper userFoundMapper;

    @Autowired
    private SendMessageUtil sendMessageUtil;

    @AngelinaGroup(keyWords = {"sql", "SQL"})
    public ReplayInfo ExecuteSql(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            List<String> text = messageInfo.getArgs();
            List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
            boolean b = AdminUtil.getSqlAdmin(messageInfo.getQq(), admins);
            String s = "您没有sql权限";
            if (b) {
                StringBuilder sql = new StringBuilder();
                for (int i= 1; i < text.size(); i++) {
                     sql.append(text.get(i));
                }
                s = executeSqlMapper.executeSql(sql.toString()).toString();
            }
            replayInfo.setReplayMessage(s);
        } else {
            replayInfo.setReplayMessage("请输入sql语句");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"群发消息"})
    public ReplayInfo sendGroupMessage(MessageInfo messageInfo) {

        List<AdminUserInfo> admins = adminUserMapper.selectAllAdmin();
        boolean b = AdminUtil.getSqlAdmin(messageInfo.getQq(), admins);
        String s = "您没有群发消息权限";
        if (b) {
            ReplayInfo replayInfo = new ReplayInfo();
            replayInfo.setReplayMessage(messageInfo.getText());
            for (String url: messageInfo.getImgUrlList()) {
                replayInfo.setReplayImg(url);
            }

            for (Long groupId : MiraiFrameUtil.messageIdMap.keySet()) {
                replayInfo.setGroupId(groupId);
                replayInfo.setLoginQQ(MiraiFrameUtil.messageIdMap.get(groupId));
                sendMessageUtil.sendGroupMsg(replayInfo);
            }
            return null;
        }
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        replayInfo.setReplayMessage(s);
        return replayInfo;
    }
}
