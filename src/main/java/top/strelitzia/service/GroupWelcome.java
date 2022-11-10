package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.message.data.ImageType;
import net.mamoe.mirai.contact.MemberPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.dao.GroupWelcomeMapper;
import top.strelitzia.dao.AdminUserMapper;
import top.strelitzia.util.AdminUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

@Service
@Slf4j
public class GroupWelcome {
    @Autowired
    private GroupWelcomeMapper groupWelcomeMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @AngelinaGroup(keyWords = {"入群欢迎"}, description = "自定义入群欢迎")
    public ReplayInfo groupWelcomeMessage(MessageInfo messageInfo){
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
	boolean sqlAdmin = AdminUtil.getSqlAdmin(messageInfo.getQq(), adminUserMapper.selectAllAdmin());
	if (messageInfo.getUserAdmin() == MemberPermission.MEMBER && !sqlAdmin){
		replayInfo.setReplayMessage("你没有权限修改入群欢迎");
		return  replayInfo;
	}
	else{
        if (messageInfo.getArgs().size()>1) {
            List<String> text = messageInfo.getArgs();
            Long groupId=messageInfo.getGroupId();
            StringBuilder welcomeMessage = new StringBuilder();
            for (int i= 1; i < text.size(); i++) {
                welcomeMessage.append(" ").append(text.get(i));
            }
            groupWelcomeMapper.insertWelcomeMessage(groupId,welcomeMessage.toString());
            replayInfo.setReplayMessage("设置成功！");
        }
        else {
            replayInfo.setReplayMessage("没有输入欢迎内容");
        }
        return replayInfo;
	}
    }

    @AngelinaGroup(keyWords = {"入群欢迎图片"}, description = "自定义入群欢迎图片")
    public ReplayInfo groupWelcomePicture(MessageInfo messageInfo){
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
	        boolean sqlAdmin = AdminUtil.getSqlAdmin(messageInfo.getQq(), adminUserMapper.selectAllAdmin());
	if (messageInfo.getUserAdmin() == MemberPermission.MEMBER && !sqlAdmin){
		replayInfo.setReplayMessage("你没有权限修改入群欢迎");
		return  replayInfo;
	}
	else{
        	List<String> urlList=messageInfo.getImgUrlList();
	        if (urlList.size()>0){
			Long groupId=messageInfo.getGroupId();
			for (int i= 0; i < urlList.size(); i++) {
                try {
		    log.info(urlList.get(i));
                    URL url = new URL(urlList.get(i));
                    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		    log.info("正在尝试保存到runFile/welcome/"+ messageInfo.getGroupId().toString());
                    FileOutputStream fos = new FileOutputStream("runFile/welcome/" + messageInfo.getGroupId().toString() +"_"+String.valueOf (i+1));
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    fos.close();
                    rbc.close();
                }catch (IOException e) {
                    replayInfo.setReplayMessage("发生错误！"+e.toString());
                    return replayInfo;
                }
            }
            groupWelcomeMapper.insertPictureNum(groupId,urlList.size());
            replayInfo.setReplayMessage("设置成功！");
            return replayInfo;
        }
        else {
            replayInfo.setReplayMessage("没有找到图片");
            return replayInfo;
        }
	}
    }
}
