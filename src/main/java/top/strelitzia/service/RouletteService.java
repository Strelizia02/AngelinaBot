package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.util.SendMessageUtil;

import java.util.*;

@Service
public class RouletteService {

    @Autowired
    SendMessageUtil sendMessageUtil;

    //轮盘赌map
    private static final Map<Long, List<Integer>> rouletteInfo = new HashMap<>();

    @AngelinaGroup(keyWords = {"给轮盘上子弹","上膛","拔枪吧"}, description = "守护铳轮盘赌，看看谁是天命之子(多颗子弹直接在后面输入数字）")
    public ReplayInfo Roulette(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        int bulletNum ;
        //判断数字
        if (messageInfo.getArgs().size()>1){
            boolean result = messageInfo.getArgs().get(1).matches("[0-9]+");
            if (!result){
                StringBuilder s = new StringBuilder();
                char[] arr=messageInfo.getArgs().get(1).toCharArray();
                for(char c :arr){
                    if (c>=48&&c<=57){
                        s.append(c - '0');
                    }
                }
                if (s.toString().equals("")){
                    replayInfo.setReplayMessage("对不起啊博士，没能理解您的意思，请务必告诉我数字呢");
                    return replayInfo;
                }
                bulletNum = Integer.parseInt(s.toString());
            }else {
                bulletNum = Integer.parseInt(messageInfo.getArgs().get(1));
            }
            if (bulletNum > 6){
                replayInfo.setReplayMessage("博士，您装入的子弹数量太多了");
                return replayInfo;
            }else if(bulletNum == 6) {
                replayInfo.setReplayMessage("博士...您是要自杀吗");
                return replayInfo;
            }
        }else {
            bulletNum = 1;
        }
        int bullet = 0;
        if (bulletNum == 1){
            //只加一个子弹
            for (int j=0;j<6;j++){
                bullet=bullet+new Random().nextInt(2);
            }
            replayInfo.setReplayMessage("（放入了 1 颗子弹）");
            sendMessageUtil.sendGroupMsg(replayInfo);
        }else {
            //加N个子弹,随机选弹仓加入子弹，则触发位置是最小的弹仓号
            List<Integer> list = new ArrayList<>();
            List<Integer> situList = new ArrayList<>(Arrays.asList(0,1,2,3,4,5));
            for(int i=0;i<bulletNum;i++){
                Integer situ = new Random().nextInt(situList.size());
                bullet = situList.get(situ);
                situList.remove(situ);
                list.add(bullet);
            }
            bullet=Collections.min(list);
            replayInfo.setReplayMessage("（放入了 "+ bulletNum +" 颗子弹）");
            sendMessageUtil.sendGroupMsg(replayInfo);
        }
        List<Integer> rouletteInitial = new ArrayList<>(Arrays.asList(bullet,0));
        rouletteInfo.put(messageInfo.getGroupId(),rouletteInitial);
        replayInfo.setReplayMessage("这是一把充满荣耀与死亡的守护铳，不幸者将再也发不出声音。勇士们啊，扣动你们的扳机！感谢Outcast提供的守护铳！");
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"轮盘赌结束"}, description = "结束轮盘赌")
    public ReplayInfo closeRouletteDuel(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        rouletteInfo.remove(messageInfo.getGroupId());
        replayInfo.setReplayMessage("轮盘赌已结束");
        return replayInfo;
    }

}
