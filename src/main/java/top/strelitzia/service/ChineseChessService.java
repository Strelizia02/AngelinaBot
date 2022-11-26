package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.container.AngelinaEventSource;
import top.angelinaBot.container.AngelinaListener;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.model.TextLine;
import top.angelinaBot.util.SendMessageUtil;
import top.strelitzia.model.chess.Board;
import top.strelitzia.model.chess.Info;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChineseChessService {

    @Autowired
    SendMessageUtil sendMessageUtil;
    
    @Autowired
    AdminUserMapper adminUserMapper;

    private final Map<Long, Board> map = new HashMap<>();

    @AngelinaGroup(keyWords = {"象棋", "下象棋"})
    public ReplayInfo chineseChess(MessageInfo messageInfo) throws IOException {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);

        if (map.containsKey(messageInfo.getGroupId())) {
            replayInfo.setReplayMessage("本群已有棋局，无法重复创建");
            return replayInfo;
        }

        replayInfo.setReplayMessage("请等待黑方加入");
        sendMessageUtil.sendGroupMsg(replayInfo);

        //先创建一个空棋局
        map.put(messageInfo.getGroupId(), null);

        AngelinaListener angelinaListener = new AngelinaListener() {
            @Override
            public boolean callback(MessageInfo message) {
                return messageInfo.getGroupId().equals(message.getGroupId()) && ("加入象棋".equals(message.getText()) || "加入".equals(message.getText()));
            }
        };

        angelinaListener.setGroupId(messageInfo.getGroupId());
        MessageInfo recall = AngelinaEventSource.waiter(angelinaListener).getMessageInfo();

        if (recall == null) {
            replayInfo.setReplayMessage("棋局超时，已退出棋局");
            map.remove(messageInfo.getGroupId());
            return replayInfo;
        }

        Long p1 = messageInfo.getQq();
        String p1Name = messageInfo.getName();
        Long p2 = recall.getQq();
        String p2Name = recall.getName();

        if (p1.equals(p2)) {
            replayInfo.setReplayMessage("你不能跟自己下棋");
            map.remove(messageInfo.getGroupId());
            return replayInfo;
        }

        Board board = new Board(p1, p2);
        map.put(messageInfo.getGroupId(), board);

        replayInfo.setReplayImg(board.toImg());
        replayInfo.setReplayMessage(null);
        sendMessageUtil.sendGroupMsg(replayInfo);

        while (true) {
            Long waiter;
            Long other;
            if (board.isRedNext()) {
                waiter = p1;
                other = p2;
            } else {
                waiter = p2;
                other = p1;
            }

            AngelinaListener chessListener = new AngelinaListener() {
                @Override
                public boolean callback(MessageInfo message) {
                    Pattern pattern = Pattern.compile("[车車馬马相象仕士将帅砲炮兵卒前中后一二三四五六七八九][车車馬马相象仕士将帅砲炮兵卒前中后一二三四五六七八九][进退平][一二三四五六七八九]");
                    Matcher matcher = pattern.matcher(message.getText());
                    return messageInfo.getGroupId().equals(message.getGroupId()) && message.getQq().equals(waiter) && (matcher.matches() || message.getText().equals("悔棋") || message.getText().equals("认输") || message.getText().equals("求和"));
                }
            };

            chessListener.setGroupId(messageInfo.getGroupId());
            MessageInfo chessRecall = AngelinaEventSource.waiter(chessListener).getMessageInfo();

            if (chessRecall == null) {
                replayInfo.setReplayMessage("棋局超时，已退出棋局");
                map.remove(messageInfo.getGroupId());
                return replayInfo;
            } else if (chessRecall.getText().equals("悔棋")) {
                Info info = board.backOff(board.getBoard(), waiter);
                replayInfo.setReplayMessage(info.toString());
                replayInfo.getReplayImg().clear();
                if (info.b) {
                    replayInfo.setReplayImg(board.toImg());
                }
                sendMessageUtil.sendGroupMsg(replayInfo);
            } else if (chessRecall.getText().equals("认输")) {
                String winner;
                if (chessRecall.getQq().equals(p1)) {
                    winner = p2Name;
                } else {
                    winner = p1Name;
                }
                replayInfo.setReplayMessage("棋局结束," + winner + "获胜");
                map.remove(messageInfo.getGroupId());
                return replayInfo;
            } else if (chessRecall.getText().equals("求和")) {
                replayInfo.setReplayMessage("对方请求和棋，是否同意？");
                replayInfo.getReplayImg().clear();
                sendMessageUtil.sendGroupMsg(replayInfo);
                
                AngelinaListener peaceListener = new AngelinaListener() {
                    @Override
                    public boolean callback(MessageInfo message) {
                        return messageInfo.getGroupId().equals(message.getGroupId()) && message.getQq.equals(other) && message.getText().equals("同意") || message.getText().equals("求和") || message.getText().equals("拒绝") || message.getText().equals("不同意");
                    }
                };

                peaceListener.setGroupId(messageInfo.getGroupId());
                MessageInfo peaceRecall = AngelinaEventSource.waiter(peaceListener).getMessageInfo();
                if (peaceRecall.getText().equals("同意") || peaceRecall.getText().equals("求和")) {
                    replayInfo.setReplayMessage("棋局结束,双方同意和棋");
                    map.remove(messageInfo.getGroupId());
                    return replayInfo;
                }
            } else {
                Info info = board.move(chessRecall.getText(), waiter);
                if (info.b) {
                    replayInfo.getReplayImg().clear();
                    replayInfo.setReplayImg(board.toImg());

                    String winner;
                    if (waiter.equals(p1)) {
                        winner = p2Name;
                    } else {
                        winner = p1Name;
                    }
                    if (!board.chessStack.isEmpty() && board.chessStack.peek().getEat() != null && board.chessStack.peek().getEat().getName() == '帅') {
                        replayInfo.setReplayMessage("棋局结束," + winner + "获胜");
                    } else {
                        replayInfo.setReplayMessage(null);
                    }
                } else {
                    replayInfo.setReplayMessage(info.toString());
                }

                map.remove(messageInfo.getGroupId());
                return replayInfo;
            }
        }
    }

    @AngelinaGroup(keyWords = {"象棋规则", "四子棋谱", "象棋玩法"})
    public ReplayInfo ChessRules(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        TextLine textLine = new TextLine();
        textLine.addString("四字棋谱法：");
        textLine.nextLine();
        textLine.addString("正常情况，采用【炮五进一】这种方式来记录棋谱,因四子棋谱是历史计法，因此从右往左数");

        textLine.nextLine();
        textLine.addString("1.移动");
        textLine.nextLine();
        textLine.addString("  1.1平");
        textLine.nextLine();
        textLine.addString("平是指平行移动到第N路");
        textLine.nextLine();
        textLine.addString("  1.2进");
        textLine.nextLine();
        textLine.addString("进是指前进N步");
        textLine.nextLine();
        textLine.addString("其中，马的进是指前进到第N路，比如【马二进三】就是第二路的马，按照(日)字前进到第三路。相士同理");
        textLine.nextLine();
        textLine.addString("  1.3退");
        textLine.nextLine();
        textLine.addString("退是指后退N步,马相士的走法同（进）");
        textLine.nextLine();

        textLine.addString("2. 标记：");
        textLine.nextLine();
        textLine.addString("  2.1 正常情况");
        textLine.nextLine();
        textLine.addString("第一个字是棋子名，第二个字指带横轴位置。");
        textLine.nextLine();
        textLine.addString("例如【炮八平五】：【炮】是棋子名，【五】是指第五路，此处指位于第五路的炮。");
        textLine.nextLine();
        textLine.addString("  2.2 重复棋子");
        textLine.nextLine();
        textLine.addString("有时多个棋子会走在同一路，此时第一个字用【前中后】表示，第二个字是棋子名。");
        textLine.nextLine();
        textLine.addString("例如【前车进五】：【前车】是指两个车在同一路的时候，前面的那一个车。");
        textLine.nextLine();
        textLine.addString("  2.3 多路多卒");
        textLine.nextLine();
        textLine.addString("有时可能有位于不同路的卒有重叠的情况，采用【四前进一】，【一后平一】来表示");
        textLine.nextLine();
        textLine.addString("【四前】代表第四路上的重复的卒中，前面那个");
        textLine.nextLine();
        textLine.addString("  2.4 四五卒：");
        textLine.nextLine();
        textLine.addString("当出现四、五个卒同路的时候，第一个卒为前卒，最后一个卒为后卒，中间的按照顺序。");
        textLine.nextLine();
        textLine.addString("如【前卒】【二卒】【三卒】【四卒】【后卒】");

        replayInfo.setReplayImg(textLine.drawImage());
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"结束象棋"})
    public ReplayInfo ChessRules(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        boolean sqlAdmin = AdminUtil.getSqlAdmin(messageInfo.getQq(), adminUserMapper.selectAllAdmin());
        if (messageInfo.getUserAdmin() == MemberPermission.MEMBER && !sqlAdmin) {
            replayInfo.setReplayMessage("仅有本群群主和管理员有权限结束棋局");
        } else {
            map.remove(messageInfo.getGroupId());
            AngelinaEventSource.remove(messageInfo.getGroupId());
            replayInfo.setReplayMessage("本群象棋已结束");
        }
        return replayInfo;
    }
}
