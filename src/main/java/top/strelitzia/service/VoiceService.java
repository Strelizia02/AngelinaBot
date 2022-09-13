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

    public static String[] voiceList = new String[]{"任命助理", "交谈1", "交谈2", "交谈3", "晋升后交谈1", "晋升后交谈2", "信赖提升后交谈1", "信赖提升后交谈2", "信赖提升后交谈3", "闲置", "干员报到", "观看作战记录", "精英化晋升1", "精英化晋升2", "编入队伍", "任命队长", "行动出发", "行动开始", "选中干员1", "选中干员2", "部署1", "部署2", "作战中1", "作战中2", "作战中3", "作战中4", "4星结束行动", "3星结束行动", "非3星结束行动", "行动失败", "进驻设施", "戳一下", "信赖触摸", "标题", "问候"};

    @AngelinaGroup(keyWords = {"语音"}, description = "查询干员的某条语音")
    public ReplayInfo getOperatorVoice(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        List<String> args = messageInfo.getArgs();


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
        String[] types = new String[]{"voice", "voice_cn", "voice_custom", "voice_en", "voice_kr"};

        if (args.size() > 1) {
            String name = args.get(1);
            String realName = nickNameMapper.selectNameByNickName(name);
            if (realName != null && !realName.equals(""))
                name = realName;

            if (args.size() > 2) {
                String voiceName = null;
                String type = null;
                for (int i = 2; i < args.size(); i++) {
                    if (map.containsKey(args.get(i))) {
                        type = map.get(args.get(i));
                    }
                    if (Arrays.asList(voiceList).contains(args.get(i))) {
                        voiceName = args.get(i);
                    }
                }

                List<String> voices = operatorInfoMapper.selectOperatorVoiceByNameAndVoice(type, name, voiceName);

                if (voice != null) {
                    replayInfo.setMp3(voice);
                }
                return replayInfo;
            } else {
                List<String> voices = operatorInfoMapper.selectOperatorVoiceByName(name);
                if (voices.size() > 0) {
                    replayInfo.setMp3(voices.get(new Random().nextInt(voices.size())));
                }
                return replayInfo;
            }
        } else {
            replayInfo.setReplayMessage("请输入干员名");
        }
        return replayInfo;
    }
}
