package top.strelitzia.service;

import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;

@Service
public class BeastService {
    private final char[] bd = {'嗷', '呜', '啊', '~'};

    @AngelinaGroup(keyWords = {"兽语加密"}, description = "输入字符加密成兽语")
    public ReplayInfo ToBeast(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            replayInfo.setReplayMessage("" + bd[3] + bd[1] + bd[0] + HexToBeast(ToHex(messageInfo.getArgs().get(1))) + bd[2]);
        } else {
            replayInfo.setReplayMessage("请输入需要加密的内容");
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"兽语解密"}, description = "输入兽语解密成字符")
    public ReplayInfo FromBeast(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (messageInfo.getArgs().size() > 1) {
            String str = messageInfo.getArgs().get(1);
            str = str.substring(3, str.length() - 1);
            replayInfo.setReplayMessage(FromHex(BeastToHex(str)));
        } else {
            replayInfo.setReplayMessage("请输入需要解密的内容");
        }
        return replayInfo;
    }

    private String ToHex(String str) {  // 字符串十六进制，不足4位补零
        char[] UBytes = str.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : UBytes) {
            StringBuilder hexB = new StringBuilder(Integer.toHexString(c));
            while (hexB.length() < 4) {
                hexB.insert(0, "0");
            }
            stringBuilder.append(hexB);
        }
        return stringBuilder.toString();
    }

    private String FromHex(String dataStr) {  // 十六进制字符串转字符串
        StringBuilder stringBuffer = new StringBuilder();
        int start = 0;

        for (int end = 4; end <= dataStr.length(); end += 4) {
            stringBuffer.append(Character.toString((char) Integer.parseInt(dataStr.substring(start, end), 16)));
            start += 4;
        }
        return stringBuffer.toString();
    }

    private String HexToBeast(String tf) {  // 十六进制转兽语
        char[] tfArr = tf.toCharArray();
        StringBuilder beast = new StringBuilder();

        for (int i = 0; i < tfArr.length; i++) {
            int k = Integer.valueOf(String.valueOf(tfArr[i]), 16) + (i % 16);
            if (k >= 16) {
                k -= 16;
            }
            beast.append(bd[k / 4]).append(bd[k % 4]);
        }
        return beast.toString();
    }

    private String BeastToHex(String encode) {
        char[] bfArr = encode.toCharArray();
        StringBuilder bf = new StringBuilder();

        for (int i = 0; i <= bfArr.length - 2; i += 2) {
            int pos1 = 0;
            int pos2 = 0;
            char c = bfArr[i];
            while (pos1 <= 3 && c != bd[pos1]) {
                pos1++;
            }
            char c2 = bfArr[i + 1];
            while (pos2 <= 3 && c2 != bd[pos2]) {
                pos2++;
            }
            int k = ((pos1 * 4) + pos2) - ((i / 2) % 16);
            if (k < 0) {
                k += 16;
            }
            bf.append(Integer.toHexString(k));
        }
        return bf.toString();
    }
}
