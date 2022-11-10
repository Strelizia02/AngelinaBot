package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaEvent;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.EventEnum;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.dao.GroupWelcomeMapper;
import top.strelitzia.util.PetPetUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;

@Service
public class PetPetService {

    @Autowired
    private PetPetUtil petPetUtil;

    @Autowired
    private GroupWelcomeMapper groupWelcomeMapper;

    @AngelinaEvent(event = EventEnum.NudgeEvent, description = "发送头像的摸头动图")
    @AngelinaGroup(keyWords = {"摸头", "摸我", "摸摸"}, description = "发送头像的摸头动图")
    public ReplayInfo PetPet(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        BufferedImage userImage = null;
        try {
            userImage = ImageIO.read(new URL("http://q.qlogo.cn/headimg_dl?dst_uin=" + messageInfo.getQq() + "&spec=100"));
            String path = "runFile/petpet/frame.gif";
            petPetUtil.getGif(path, userImage);
            replayInfo.setReplayImg(new File(path));
            return replayInfo;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @AngelinaEvent(event = EventEnum.GroupRecall, description = "撤回事件回复")
    public ReplayInfo GroupRecall(MessageInfo messageInfo) {
        ReplayInfo replayInfo = PetPet(messageInfo);
        replayInfo.setReplayMessage("谁撤回了消息，让我看看！");
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"口我", "透透"}, description = "禁言功能")
    public ReplayInfo MuteSomeOne(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        replayInfo.setMuted((new Random().nextInt(5) + 1) * 60);
        return replayInfo;
    }

    @AngelinaEvent(event = EventEnum.MemberJoinEvent, description = "入群欢迎")
    public ReplayInfo memberJoin(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        Long groupId =messageInfo.getGroupId();
	if (groupWelcomeMapper.getWelcomeMessage(groupId)!=null){
		replayInfo.setReplayMessage("欢迎" + messageInfo.getName()
                    + "入群，请通过【龟龟菜单】了解洁哥的功能。"
                    + "\n龟龟原项目源码：https://github.com/Strelizia02/AngelinaBot\n"
                    + groupWelcomeMapper.getWelcomeMessage(messageInfo.getGroupId()));
	}
	else {
		replayInfo.setReplayMessage("欢迎" + messageInfo.getName()
                    + "入群，请通过【龟龟菜单】了解洁哥的功能。"
                    + "\n龟龟原项目源码：https://github.com/Strelizia02/AngelinaBot");
	}
	if (groupWelcomeMapper.getPictureNum(groupId)!=null&&groupWelcomeMapper.getPictureNum(groupId)!=0){
		for (int i=1;i<=groupWelcomeMapper.getPictureNum((groupId));i++){
			File file =new File("runFile/welcome/" + groupId.toString() +"_"+String.valueOf (i));
			replayInfo.setReplayImg(file);
		}
	}
        return replayInfo;
    }

    @AngelinaEvent(event = EventEnum.MemberLeaveEvent, description = "退群提醒")
    public ReplayInfo memberLeaven(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        replayInfo.setReplayMessage("有成员离开了我们");
        return replayInfo;
    }
}
