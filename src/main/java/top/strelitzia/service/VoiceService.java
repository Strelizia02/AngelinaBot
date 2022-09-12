package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.strelitzia.arknightsDao.OperatorInfoMapper;
import top.strelitzia.dao.NickNameMapper;

import java.util.*;

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
    public ReplayInfo getOperatorVoice(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        String name = messageInfo.getArgs().get(1);
        String realName = nickNameMapper.selectNameByNickName(name);
        if (realName != null && !realName.equals(""))
            name = realName;

        Map<String, String> map = new HashMap<>();
        map.put("中配", "voice_cn");
        map.put("中文", "voice_cn");

        map.put("方言", "voice_custom");

        map.put("英语", "voice_en");
        map.put("英配", "voice_en");

        map.put("韩配", "voice_kr");
        map.put("韩语", "voice_kr");

        map.put("日语", "voice");
        map.put("原配", "voice");

        if (messageInfo.getArgs().size() > 3) {
            String type = map.get(messageInfo.getArgs().get(2));
            String voice = operatorInfoMapper.selectOperatorVoiceByNameAndVoice(type, name, messageInfo.getArgs().get(2));
            if (voice != null) {
                replayInfo.setMp3(voice);
            }
            return replayInfo;
        } else if (messageInfo.getArgs().size() > 2) {
            String[] types = new String[]{"voice", "voice_cn", "voice_custom", "voice_en", "voice_kr"};
            String voice = operatorInfoMapper.selectOperatorVoiceByNameAndVoice(types[new Random().nextInt(types.length)], name, messageInfo.getArgs().get(2));
            if (voice != null) {
                replayInfo.setMp3(voice);
            }
            return replayInfo;
        } else if (messageInfo.getArgs().size() > 1) {
            List<String> voices = operatorInfoMapper.selectOperatorVoiceByName(name);
            if (voices.size() > 0) {
                replayInfo.setMp3(voices.get(new Random().nextInt(voices.size())));
            }
            return replayInfo;
        } else {
            replayInfo.setReplayMessage("请输入干员名");
        }
        return replayInfo;
    }
}
