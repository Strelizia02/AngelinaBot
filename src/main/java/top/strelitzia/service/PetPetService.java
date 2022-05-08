package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaEvent;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.EventEnum;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.util.ImageUtil;
import top.strelitzia.util.PetPetUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

@Service
public class PetPetService {

    @Autowired
    private PetPetUtil petPetUtil;

    @Autowired
    private ImageUtil imageUtil;

    @AngelinaEvent(event = EventEnum.NudgeEvent, description = "发送头像的摸头动图")
    @AngelinaGroup(keyWords = {"摸头", "摸我", "摸摸"}, description = "发送头像的摸头动图")
    public ReplayInfo PetPet(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        BufferedImage userImage = ImageUtil.Base64ToImageBuffer(
                ImageUtil.getImageBase64ByUrl("http://q.qlogo.cn/headimg_dl?dst_uin=" + messageInfo.getQq() + "&spec=100"));
        String path = "runFile/petpet/frame.gif";
        petPetUtil.getGif(path, userImage);
        replayInfo.setReplayImg(new File(path));
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
        replayInfo.setReplayMessage("欢迎" + messageInfo.getName() + "入群，请通过【洁哥菜单】了解洁哥的功能。");
        return replayInfo;
    }

    @AngelinaEvent(event = EventEnum.MemberLeaveEvent, description = "退群提醒")
    public ReplayInfo memberLeaven(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        replayInfo.setReplayMessage(messageInfo.getName() + "离开了");
        return replayInfo;
    }
}
