package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.arknightsDao.OperatorInfoMapper;
import top.strelitzia.dao.NickNameMapper;

import java.util.List;
import java.util.Random;

/**
 * @author strelitzia
 * @Date 2022/09/03 14:38
 **/
@Service
public class VoiceService {

    @Autowired
    private OperatorInfoMapper operatorInfoMapper;

    @Autowired
    private NickNameMapper nickNameMapper;

    @AngelinaGroup(keyWords = {"语音"}, description = "查询干员的某条语音")
    public ReplayInfo getOperatorSkinByInfo(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            //干员名或者技能名
            String name = messageInfo.getArgs().get(1);

            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals(""))
                name = realName;

            List<String> voices = operatorInfoMapper.selectOperatorVoiceByName(name);
            if (voices.size() > 0) {
                replayInfo.setMp3(voices.get(new Random().nextInt(voices.size())));
            }
        } else if (messageInfo.getArgs().size() > 2) {
            String name = messageInfo.getArgs().get(1);

            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals(""))
                name = realName;

            String voice = operatorInfoMapper.selectOperatorVoiceByNameAndVoice(name, messageInfo.getArgs().get(2));
            if (voice != null) {
                replayInfo.setMp3(voice);
            }
        } else {
            replayInfo.setReplayMessage("请输入干员名");
        }
        return replayInfo;
    }
}
