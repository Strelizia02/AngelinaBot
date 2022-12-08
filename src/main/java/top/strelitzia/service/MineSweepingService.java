package top.strelitzia.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.container.AngelinaEventSource;
import top.angelinaBot.container.AngelinaListener;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.util.SendMessageUtil;
import top.strelitzia.model.minesweeping.Info;
import top.strelitzia.model.minesweeping.MineSweeping;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MineSweepingService {

    @Autowired
    SendMessageUtil sendMessageUtil;

    private static final Set<Long> groupList = new HashSet<>();

    @AngelinaGroup(keyWords = {"扫雷"})
    public ReplayInfo mineSweeping(MessageInfo messageInfo) throws IOException {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);

        if (groupList.contains(messageInfo.getGroupId())) {
            replayInfo.setReplayMessage("本群正在进行扫雷，请查看消息记录");
            return replayInfo;
        } else {
            groupList.add(messageInfo.getGroupId());

            MineSweeping mineSweeping;
            if (messageInfo.getArgs().size() > 1) {
                if (messageInfo.getArgs().get(1).equals("中级")) {
                    mineSweeping = new MineSweeping(2);
                } else if (messageInfo.getArgs().get(1).equals("高级")) {
                    mineSweeping = new MineSweeping(3);
                } else {
                    mineSweeping = new MineSweeping();
                }
            } else {
                mineSweeping = new MineSweeping();
            }

            replayInfo.setReplayMessage("请在10秒内选择模式：\n[1]个人模式 仅自己玩\n[2]团体模式 群友一起玩");
            sendMessageUtil.sendGroupMsg(replayInfo);
            replayInfo.setReplayMessage(null);

            AngelinaListener angelinaListener = new AngelinaListener() {
                @Override
                public boolean callback(MessageInfo message) {
                    return message.getGroupId().equals(messageInfo.getGroupId()) &&
                            (message.getText().equals("1") || message.getText().equals("2") ||
                                message.getText().equals("个人") || message.getText().equals("团体") ||
                                message.getText().equals("个人模式") || message.getText().equals("团体模式"));
                }
            };
            angelinaListener.setGroupId(messageInfo.getGroupId());
            angelinaListener.setSecond(10);
            MessageInfo recall = AngelinaEventSource.waiter(angelinaListener).getMessageInfo();

            boolean isOnePlayer = true;
            long qq = messageInfo.getQq();
            if (recall == null) {
                replayInfo.setReplayMessage("超时未选择，默认以个人模式开启，本群扫雷正式开始");
            } else if (recall.getText().equals("2") || recall.getText().equals("团体") || recall.getText().equals("团体模式")) {
                isOnePlayer = false;
            }
            replayInfo.setReplayMessage("本群扫雷正式开始");
            replayInfo.setReplayImg(mineSweeping.toImg());
            sendMessageUtil.sendGroupMsg(replayInfo);
            replayInfo.setReplayMessage(null);
            replayInfo.getReplayImg().clear();

            boolean goOn = true;
            while (goOn) {
                boolean finalIsOnePlayer = isOnePlayer;
                AngelinaListener mineListener = new AngelinaListener() {
                    @Override
                    public boolean callback(MessageInfo message) {
                        Pattern pattern = Pattern.compile("(标记)?[0-9*].[0-9*]");
                        Matcher matcher = pattern.matcher(message.getText());
                        return message.getGroupId().equals(messageInfo.getGroupId()) && (!finalIsOnePlayer || message.getQq().equals(qq)) &&
                                matcher.matches();
                    }
                };
                mineListener.setGroupId(messageInfo.getGroupId());
                MessageInfo mine = AngelinaEventSource.waiter(mineListener).getMessageInfo();
                if (mine == null) {
                    replayInfo.setReplayMessage("扫雷超时已结束");
                    groupList.remove(messageInfo.getGroupId());
                    return replayInfo;
                }

                if (mine.getText().contains("标记")) {
                    String[] split = mine.getText().replace("标记", "").split("\\.");
                    int x = Integer.parseInt(split[0]);
                    int y = Integer.parseInt(split[1]);
                    mineSweeping.flag(x, y);
                    replayInfo.setReplayImg(mineSweeping.toImg());
                    sendMessageUtil.sendGroupMsg(replayInfo);
                    replayInfo.setReplayMessage(null);
                    replayInfo.getReplayImg().clear();
                } else {
                    String[] split = mine.getText().split("\\.");
                    int x = Integer.parseInt(split[0]);
                    int y = Integer.parseInt(split[1]);

                    Info choose = mineSweeping.choose(x, y);

                    if (choose.b) {
                        replayInfo.setReplayImg(mineSweeping.toImg());
                        replayInfo.setReplayMessage(choose.toString());
                    } else {
                        replayInfo.setReplayImg(mineSweeping.toImgOver());
                        replayInfo.setReplayMessage(choose.toString());
                        groupList.remove(messageInfo.getGroupId());
                        goOn = false;
                    }
                    sendMessageUtil.sendGroupMsg(replayInfo);
                    replayInfo.setReplayMessage(null);
                    replayInfo.getReplayImg().clear();
                }
            }
        }
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"重启扫雷"})
    public ReplayInfo reMineSweeping(MessageInfo messageInfo) throws IOException {
        groupList.remove(messageInfo.getGroupId());
        AngelinaEventSource.remove(messageInfo.getGroupId());
        return mineSweeping(messageInfo);
    }

    @AngelinaGroup(keyWords = {"结束扫雷", "终止扫雷", "中止扫雷"})
    public ReplayInfo stopMineSweeping(MessageInfo messageInfo) {
        groupList.remove(messageInfo.getGroupId());
        AngelinaEventSource.remove(messageInfo.getGroupId());
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        replayInfo.setReplayMessage("本群猜干员已结束");
        return replayInfo;
    }
    
    @AngelinaGroup(keyWords = {"组队扫雷", "扫雷小队"})
    public ReplayInfo stopMineSweeping(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        if (groupList.contains(messageInfo.getGroupId())) {
            replayInfo.setReplayMessage("本群正在进行扫雷，请查看消息记录");
            return replayInfo;
        } else {
            groupList.add(messageInfo.getGroupId());
            Queue<Long> playerQueue = new LinkedList<>();
            Queue<String> playerNameQueue = new LinkedList<>();
            playerQueue.offer(messageInfo.getQq());
            playerNameQueue.offer(messageInfo.getName());

            replayInfo.setReplayMessage("请在15秒内加入扫雷小队");
            sendMessageUtil.sendGroupMsg(replayInfo);
            replayInfo.setReplayMessage(null);
            
            int num = 5;
            if (messageInfo.getArgs().size() > 1) {
                try {
                    num = Integet.parseInt(messageInfo.getArgs().get(1));
                } catch (NumberFormatException e) {
                    log.error(e);
                }
                if (2 < num || num > 10) {
                    replayInfo.setReplayMessage("仅支持2-10个人");
                    return replayInfo;
                }
            }

            for (int i = 0; i < num; i++) {
                AngelinaListener angelinaListener = new AngelinaListener() {
                    @Override
                    public boolean callback(MessageInfo message) {
                        return message.getGroupId().equals(messageInfo.getGroupId()) && message.getText().equals("加入");
                    }
                };
                angelinaListener.setGroupId(messageInfo.getGroupId());
                angelinaListener.setSecond(15);

                MessageInfo recall = AngelinaEventSource.waiter(angelinaListener).getMessageInfo();

                if (recall == null && playerQueue.size() <= 1) {
                    groupList.remove(messageInfo.getGroupId());
                    replayInfo.setReplayMessage("无人加入，扫雷小队已终止");
                    return replayInfo;
                } else if (recall == null) {
                    replayInfo.setReplayMessage("等待超时，即将开始游戏");
                    sendMessageUtil.sendGroupMsg(replayInfo);
                    replayInfo.setReplayMessage(null);
                    break;
                } else {
                    playerQueue.offer(recall.getQq());
                    playerNameQueue.offer(recall.getName());
                    StringBuilder sb = new StringBuilder("加入成功，当前小队成员：");
                    int i = 0;
                    for (String name: playerNameQueue) {
                        i++;
                        sb.append("\n【").append(i).append("】").append(name);
                    }
                    replayInfo.setReplayMessage(sb.toString());
                    sendMessageUtil.sendGroupMsg(replayInfo);
                    replayInfo.setReplayMessage(null);
                }
            }

            MineSweeping mineSweeping = new MineSweeping();

            replayInfo.setReplayMessage("请" + playerNameQueue.peek() + "开始扫雷");
            replayInfo.setReplayImg(mineSweeping.toImg());
            sendMessageUtil.sendGroupMsg(replayInfo);
            replayInfo.setReplayMessage(null);
            replayInfo.getReplayImg().clear();

            while (playerQueue.size() != 1) {
                AngelinaListener mineListener = new AngelinaListener() {
                    @Override
                    public boolean callback(MessageInfo message) {
                        Pattern pattern = Pattern.compile("[0-9*].[0-9*]");
                        Matcher matcher = pattern.matcher(message.getText());
                        return message.getGroupId().equals(messageInfo.getGroupId()) && playerQueue.peek().equals(message.getQq()) &&
                                matcher.matches();
                    }
                };
                mineListener.setGroupId(messageInfo.getGroupId());
                MessageInfo mine = AngelinaEventSource.waiter(mineListener).getMessageInfo();

                if (mine == null) {
                    playerQueue.poll();
                    playerNameQueue.poll();

                    replayInfo.setReplayMessage("您已超时");
                    replayInfo.setReplayImg(mineSweeping.toImg());
                    replayInfo.setMuted((new Random().nextInt(5) + 1) * 60);
                    replayInfo.setQq(mine.getQq());
                    sendMessageUtil.sendGroupMsg(replayInfo);
                    replayInfo.setReplayMessage(null);
                    replayInfo.getReplayImg().clear();
                    replayInfo.setMuted(null);
                } else {
                    String[] split = mine.getText().split("\\.");
                    int x = Integer.parseInt(split[0]);
                    int y = Integer.parseInt(split[1]);

                    Info choose = mineSweeping.choose(x, y);

                    if (choose.b) {
                        playerQueue.offer(playerQueue.poll());
                        playerNameQueue.offer(playerNameQueue.poll());

                        replayInfo.setReplayMessage("请" + playerNameQueue.peek() + "开始扫雷");
                        replayInfo.setReplayImg(mineSweeping.toImg());
                        replayInfo.setReplayMessage(choose.toString());
                        sendMessageUtil.sendGroupMsg(replayInfo);
                        replayInfo.setReplayMessage(null);
                        replayInfo.getReplayImg().clear();

                    } else if (choose.message.equals("victory")) {
                        break;
                    } else if (choose.message.equals("is already open")) {
                        replayInfo.setReplayMessage(playerNameQueue.peek() + "不能选择已翻开的各自，请重新扫雷");
                        sendMessageUtil.sendGroupMsg(replayInfo);
                        replayInfo.setReplayMessage(null);
                    } else {
                        Long qq = playerQueue.poll();
                        String dead = playerNameQueue.poll();

                        replayInfo.setReplayMessage(dead + "已阵亡，请" + playerNameQueue.peek() + "开始扫雷");
                        replayInfo.setReplayImg(mineSweeping.toImg());
                        replayInfo.setReplayMessage(choose.toString());

                        replayInfo.setMuted((new Random().nextInt(5) + 1) * 60);
                        replayInfo.setQq(qq);
                        sendMessageUtil.sendGroupMsg(replayInfo);
                        replayInfo.setMuted(null);
                        replayInfo.setReplayMessage(null);
                        replayInfo.getReplayImg().clear();
                    }
                }
            }

            playerQueue.poll();
            String victory = playerNameQueue.poll();

            replayInfo.setReplayMessage("胜利者是" + victory);

            replayInfo.setReplayImg(mineSweeping.toImgOver());
            sendMessageUtil.sendGroupMsg(replayInfo);
            replayInfo.setReplayMessage(null);
            replayInfo.getReplayImg().clear();

            for (Long qq : playerQueue) {
                replayInfo.setMuted((new Random().nextInt(5) + 1) * 60);
                replayInfo.setQq(qq);
                sendMessageUtil.sendGroupMsg(replayInfo);
                replayInfo.setMuted(null);
            }
            return null;
        }
    }
}
