package top.strelitzia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.container.AngelinaEventSource;
import top.angelinaBot.container.AngelinaListener;
import top.angelinaBot.model.*;
import top.angelinaBot.util.SendMessageUtil;
import top.strelitzia.dao.AdminUserMapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Cuthbert
 * @Date 2022/7/19 22:29
 **/
@Service
public class GobangService {

    private static final Set<String> groupList = new HashSet<>();

    @Autowired
    SendMessageUtil sendMessageUtil;

    @Autowired
    AdminUserMapper adminUserMapper;

    @AngelinaGroup(keyWords = {"重置棋盘" , "重置五子棋"}, funcClass = FunctionType.Others, permission = PermissionEnum.GroupAdministrator, author = "Cuthbert-yong")
    public ReplayInfo resetGobang(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        groupList.remove(messageInfo.getGroupId());
        //TODO 删掉listener
        replayInfo.setReplayMessage("重置完成");
        return replayInfo;
    }


    @AngelinaGroup(keyWords = {"五子棋"}, funcClass = FunctionType.ArknightsData, author = "Cuthbert-yong")
    public ReplayInfo gobang(MessageInfo messageInfo) throws IOException {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        String groupId = messageInfo.getGroupId();
        if (groupList.contains(groupId)) {
            replayInfo.setReplayMessage("本群正在进行五子棋，或五子棋匹配中" +
                    "\n加入请发送“加入五子棋”或“洁哥加入五子棋”" +
                    "\n请如果遇到问题可以发送“洁哥重置棋盘”");
            return replayInfo;
        } else {
            replayInfo.setReplayMessage(messageInfo.getName() + "加入成功！\n" + "[五子棋]正在等待二号玩家的加入......");
            sendMessageUtil.sendGroupMsg(replayInfo);
            replayInfo.setReplayMessage(null);
            groupList.add(messageInfo.getGroupId());
            //等待二号玩家加入
            AngelinaListener playerListener = new AngelinaListener() {
                @Override
                public boolean callback(MessageInfo message) {
                    return message.getGroupId().equals(messageInfo.getGroupId()) &&
                            (message.getText().equals("加入五子棋") || message.getText().equals("洁哥加入五子棋") ||
                                    message.getText().equals("取消") || message.getText().equals("取消匹配"));
                }
            };
            playerListener.setGroupId(messageInfo.getGroupId());
            MessageInfo player2 = AngelinaEventSource.waiter(playerListener).getMessageInfo();
            if (player2 == null) {
                replayInfo.setReplayMessage(messageInfo.getName() + " 匹配超时\n[五子棋]已重置");
                groupList.remove(messageInfo.getGroupId());
                return replayInfo;
            }
            
            if (player2.getQq().equals(messageInfo.getQq())) {
                replayInfo.setReplayMessage("自娱自乐是吧？达咩！");
                groupList.remove(messageInfo.getGroupId());
                return replayInfo;
            }
            if ((player2.getText().equals("取消") || player2.getText().equals("取消匹配")) &&
                    player2.getQq().equals(messageInfo.getQq())){
                replayInfo.setReplayMessage(messageInfo.getName() + " 取消匹配\n[五子棋]已重置");
                groupList.remove(messageInfo.getGroupId());
                return replayInfo;
            }

            String circleQq;
            String squareQq;
            String circleName;
            String squareName;
            int r = new Random().nextInt(2);
            if (r == 0) {
                circleQq = messageInfo.getQq();
                circleName = messageInfo.getName();
                squareQq = player2.getQq();
                squareName = player2.getName();
            } else {
                squareQq = messageInfo.getQq();
                squareName = messageInfo.getName();
                circleQq = player2.getQq();
                circleName = player2.getName();
            }

            replayInfo.setReplayMessage(player2.getName() + "加入成功！\n" + "[五子棋]即将开始");
            sendMessageUtil.sendGroupMsg(replayInfo);
            replayInfo.setReplayMessage(null);
            //初始化circleTurn用于回合判定，surrender用于判断是否认输，result用于判定结果
            boolean circleTurn = true;
            boolean surrender = false;
            int result = 0;
            //生成一张棋盘并发送
            int[][] board = new int[15][15];
            BigDecimal lastPiece = null;

            replayInfo.setReplayImg(DrawBoard(board, circleTurn, circleName, squareName, lastPiece));
            sendMessageUtil.sendGroupMsg(replayInfo);
            replayInfo.getReplayImg().clear();

            //进入循环
            for(int i = 0 ;  i<256 ;i++) {
                //listener等待指令
                AngelinaListener angelinaListener = new AngelinaListener() {
                    @Override
                    public boolean callback(MessageInfo message) {
                        if (message.getText().equals("棋盘") || message.getText().equals("查看棋盘")) {
                            return true;
                            //接收本群参赛者发送的坐标，或重置指令
                        }else if (message.getGroupId().equals(messageInfo.getGroupId()) &&
                                (message.getQq().equals(circleQq) || message.getQq().equals(squareQq))) {
                            if (isNumber(message.getText())) {
                                return true;
                            }else {
                                return message.getText().equals("重置") || message.getText().equals("重置棋盘")
                                        || message.getText().equals("投降") || message.getText().equals("认输")
                                        || message.getText().equals("悔棋");
                            }
                        } else {
                            //接收群主和管理员以及sql发送的重置指令
                            return (message.getGroupId().equals(messageInfo.getGroupId()) &&
                                    (message.getUserAdmin() != PermissionEnum.GroupUser) &&
                                    (message.getText().equals("重置") || message.getText().equals("重置棋盘")));
                        }
                    }
                };
                angelinaListener.setGroupId(groupId);
                MessageInfo recall = AngelinaEventSource.waiter(angelinaListener).getMessageInfo();
                if (recall == null) {
                    groupList.remove(messageInfo.getGroupId());
                    replayInfo.setReplayMessage("落子超时，本群棋盘已重置");
                    return replayInfo;
                }

                if (recall.getText().equals("重置") || recall.getText().equals("重置棋盘")) {
                    groupList.remove(messageInfo.getGroupId());
                    replayInfo.setReplayMessage("本群棋盘已重置");
                    return replayInfo;
                }

                if (recall.getText().equals("棋盘") || recall.getText().equals("查看棋盘")) {
                    replayInfo.setReplayImg(DrawBoard(board, circleTurn, circleName, squareName, lastPiece));
                    sendMessageUtil.sendGroupMsg(replayInfo);
                    replayInfo.getReplayImg().clear();
                    i--;
                    continue;
                }

                if (recall.getText().equals("投降") || recall.getText().equals("认输")){
                    if (recall.getQq().equals(circleQq)){
                        surrender = true;
                        result = 2;
                        break;
                    }else if (recall.getQq().equals(squareQq)){
                        surrender = true;
                        result = 1;
                        break;
                    }
                }

                if (recall.getText().equals("悔棋")){
                    if (recall.getQq().equals(circleQq) || recall.getQq().equals(squareQq)) {
                        if (circleTurn && recall.getQq().equals(squareQq)) {
                            if (lastPiece == null) {
                                replayInfo.setReplayMessage("不能连续悔棋哦");
                                sendMessageUtil.sendGroupMsg(replayInfo);
                                replayInfo.setReplayMessage(null);
                                i--;
                                continue;
                            }
                            BigDecimal xy = lastPiece;
                            int x = getX(xy)-1;
                            int y = getY(xy)-1;
                            board[x][y] = 0;
                            lastPiece = null;
                            replayInfo.setReplayMessage("■悔棋");
                            replayInfo.setReplayImg(DrawBoard(board, circleTurn, circleName, squareName, lastPiece));
                            sendMessageUtil.sendGroupMsg(replayInfo);
                            replayInfo.setReplayMessage(null);
                            replayInfo.getReplayImg().clear();
                            circleTurn = false;
                            i -= 2;
                            continue;
                        } else if (!circleTurn && recall.getQq().equals(circleQq)) {
                            if (lastPiece == null) {
                                replayInfo.setReplayMessage("不能连续悔棋哦");
                                sendMessageUtil.sendGroupMsg(replayInfo);
                                replayInfo.setReplayMessage(null);
                                i--;
                                continue;
                            }
                            BigDecimal xy = lastPiece;
                            int x = getX(xy)-1;
                            int y = getY(xy)-1;
                            board[x][y] = 0;
                            lastPiece = null;
                            replayInfo.setReplayMessage("●悔棋");
                            replayInfo.setReplayImg(DrawBoard(board, circleTurn, circleName, squareName, lastPiece));
                            sendMessageUtil.sendGroupMsg(replayInfo);
                            replayInfo.setReplayMessage(null);
                            replayInfo.getReplayImg().clear();
                            circleTurn = true;
                            i -= 2;
                            continue;
                        }else {
                            replayInfo.setReplayMessage("只有在自己的回合才能进行悔棋哦");
                            sendMessageUtil.sendGroupMsg(replayInfo);
                            replayInfo.setReplayMessage(null);
                            i--;
                            continue;
                        }
                    }

                }


                //汉字指令在上面处理完毕，获取坐标
                BigDecimal xy;
                //如果为圆圈的回合
                if (circleTurn && recall.getQq().equals(circleQq)) {
                    try{
                        xy = new BigDecimal(recall.getText());
                    }catch (NumberFormatException e){
                        replayInfo.setReplayMessage("错误的落子指令\n指令应形如4.2或13.14（小数点前为行，小数点后为列），且行与列均属于范围[1,15]");
                        sendMessageUtil.sendGroupMsg(replayInfo);
                        replayInfo.setReplayMessage(null);
                        i--;
                        continue;
                    }
                    if (xy.compareTo(BigDecimal.valueOf(getX(xy)))==0 || !validXY(xy)){
                        replayInfo.setReplayMessage("错误的落子指令\n指令应形如4.2或13.14（小数点前为行，小数点后为列），且行与列均属于范围[1,15]");
                        sendMessageUtil.sendGroupMsg(replayInfo);
                        replayInfo.setReplayMessage(null);
                        i--;
                        continue;
                    }
                    //解析坐标
                    int x = getX(xy)-1;
                    int y = getY(xy)-1;
                    //判断落点是否有效
                    if (board[x][y] != 0){
                        replayInfo.setReplayMessage("无效落子●");
                        sendMessageUtil.sendGroupMsg(replayInfo);
                        replayInfo.setReplayMessage(null);
                        i--;
                    }else {
                        board[x][y] = 1;
                        lastPiece = xy;
                        //判断是否五连,是则跳出循环
                        if (GobangOver(xy, board)) {
                            result = 1;
                            break;
                        }else {
                            //没有五连，更改turn并发送棋盘
                            circleTurn = false;
                            replayInfo.setReplayImg(DrawBoard(board, circleTurn, circleName, squareName, lastPiece));
                            sendMessageUtil.sendGroupMsg(replayInfo);
                            replayInfo.setReplayMessage(null);
                            replayInfo.getReplayImg().clear();
                        }
                    }
                }else
                    //如果为方块的回合
                    if (!circleTurn && recall.getQq().equals(squareQq)) {
                        try{
                            xy = new BigDecimal(recall.getText());
                        }catch (NumberFormatException e){
                            replayInfo.setReplayMessage("错误的落子指令\n指令应形如4.2或13.14（小数点前为行，小数点后为列），且行与列均属于范围[1,15]");
                            sendMessageUtil.sendGroupMsg(replayInfo);
                            replayInfo.setReplayMessage(null);
                            i--;
                            continue;
                        }
                        if (xy.compareTo(BigDecimal.valueOf(getX(xy)))==0 || !validXY(xy)){
                            replayInfo.setReplayMessage("错误的落子指令\n指令应形如4.2或13.14（小数点前为行，小数点后为列），且行与列均属于范围[1,15]");
                            sendMessageUtil.sendGroupMsg(replayInfo);
                            replayInfo.setReplayMessage(null);
                            i--;
                            continue;
                        }
                        //解析坐标
                        int x = getX(xy)-1;
                        int y = getY(xy)-1;
                        //判断落点是否有效
                        if (board[x][y] != 0){
                            replayInfo.setReplayMessage("无效落子■");
                            sendMessageUtil.sendGroupMsg(replayInfo);
                            replayInfo.setReplayMessage(null);
                            i--;
                        }else {
                            //将行写入board，并记录落子
                            board[x][y] = 2;
                            lastPiece = xy;
                            //判断是否五连,是则跳出循环
                            if (GobangOver(xy, board)) {
                                result = 2;
                                break;
                            }else {
                                //没有五连，更改turn并发送棋盘
                                circleTurn = true;
                                replayInfo.setReplayImg(DrawBoard(board, circleTurn, circleName, squareName, lastPiece));
                                sendMessageUtil.sendGroupMsg(replayInfo);
                                replayInfo.getReplayImg().clear();
                            }
                        }
                    }
            }

            //循环结束
            TextLine textLine = new TextLine();
            switch (result){
                case 1:
                    //圆圈获胜
                    if (surrender){
                        textLine.addCenterStringLine("■认输");
                        textLine.nextLine();
                    }
                    textLine.addString("对局结束，获胜者是：");
                    textLine.nextLine();
                    textLine.addString(circleName);
                    textLine.nextLine();
                    textLine.addString("恭喜！");
                    replayInfo.setReplayImg(textLine.drawImage());
                    groupList.remove(messageInfo.getGroupId());
                    return replayInfo;
                case 2:
                    //方块获胜
                    if (surrender){
                        textLine.addCenterStringLine("●认输");
                        textLine.nextLine();
                    }
                    textLine.addString("对局结束，获胜者是：");
                    textLine.nextLine();
                    textLine.addString(squareName);
                    textLine.nextLine();
                    textLine.addString("恭喜！");
                    replayInfo.setReplayImg(textLine.drawImage());
                    groupList.remove(messageInfo.getGroupId());
                    return replayInfo;
                default:
                    //下满了
                    textLine.addString("对局结束，无人获胜");
                    textLine.nextLine();
                    textLine.addString("不得不佩服，");
                    if (circleQq.equals(squareQq)) {
                        textLine.addString("您很有耐心，");
                    }else {
                        textLine.addString("您二位很有耐心，");
                    }
                    textLine.addString("令人感慨");
                    textLine.nextLine();
                    textLine.addString("不过有这些时间，做点更有意义的事情，比如读一本好书，或者陪陪家人而不是陪我一个冷冰冰的bot，会不会更好呢？");
                    replayInfo.setReplayImg(textLine.drawImage());
                    groupList.remove(messageInfo.getGroupId());
                    return replayInfo;
            }
        }
    }

    public BufferedImage DrawBoard(int[][] board,Boolean circleTurn,String circleName,String squareName,BigDecimal lastPiece) throws IOException {
        BufferedImage img = new BufferedImage(1080,3541*1080/2508,BufferedImage.TYPE_INT_BGR);
        Graphics2D g = (Graphics2D) img.getGraphics();
        InputStream is = null;
        File file = new File("runFile/gobang");
        //如果文件夹不存在则创建
        if (!file.exists() && !file.isDirectory()) file.mkdirs();

        file = new File("runFile/gobang/material.jpg");
        if (file.exists()) {
            is = Files.newInputStream(Paths.get("runFile/gobang/material.jpg"));
            g.drawImage(ImageIO.read(is), 0, 0, 1080, 1524, null);
            g.dispose();
        }else {
            //填充背景
            File bg = new File("runFile/gobang/background.jpg");
            if (!bg.exists()) downloadOneFile("runFile/gobang/background.jpg","http://r.photo.store.qq.com/psc?/V53NeKT03xtuqS1NsbU61gGDhB2oURWx/6tCTPh7N*X6CBkvkDvKlZewyZoKm3FNlLZP2UROKKNQjv8vMO7BNGIwJjItKaA1b7QbjPba9ZrigkigcwriNs9usLnFG3YqdOCB2wUGiZBg!/r");
            is = Files.newInputStream(Paths.get("runFile/gobang/background.jpg"));
            g.drawImage(ImageIO.read(is), 0, 0, 1080, 1524, null);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(255, 255, 255, 163));
            g.fillRect(74, 445, 937, 929);
            g.fillRect(472, 85, 136, 50);//标题的背景

            g.setFont(new Font("楷体", Font.BOLD, 40));
            g.setColor(Color.BLACK);
            g.drawString("五子棋",477,125);
            //循环划线
            for(int i=0;i<15;i++){
                g.fillRect(134,499+i*60,842,2);
                g.fillRect(134+i*60,499,2,842);
                if (i<9){
                    //添加左侧数字
                    g.drawString(String.valueOf(i+1),99,499+i*60+18);
                    //添加上方数字
                    g.drawString(String.valueOf(i+1),134+i*60-10,489);
                }else {
                    //添加左侧数字
                    g.drawString(String.valueOf(i+1),79,499+i*60+18);
                    //添加上方数字
                    g.drawString(String.valueOf(i+1),134+i*60-25,489);
                }
            }

            g.setFont(new Font("楷体", Font.BOLD, 20));
            g.drawString("安洁莉娜Bot开源 https://github.com/Strelizia02/AngelinaBot", 380, 1440);


            //获取logo
            is = new ClassPathResource("/pic/logo.jpg").getInputStream();
            g.drawImage(ImageIO.read(is), 1055, 0, 25 , 25 , null);
            g.dispose();

            ImageIO.write(img,"jpg",file);
        }

        g = (Graphics2D) img.getGraphics();

        String str = "上枚落子坐标为" + lastPiece;
        g.setColor(new Color(255, 255, 255, 163));
        g.fillRect(74, 225, circleName.length()*42+55, 50);
        g.fillRect(74, 295, squareName.length()*42+55, 50);
        g.fillRect(74, 360, 290, 50);//现在是谁的回合 的背景
        if (lastPiece!=null) g.fillRect(540, 360, str.length()*42-42, 50);

        g.setColor(new Color(145, 120, 99, 255));
        g.fillOval(79,230,40,40);
        if (circleTurn) g.fillOval(220,365,40,40);

        g.setColor(new Color(156, 192, 102, 255));
        g.fillOval(79,300,40,40);
        if (!circleTurn) g.fillOval(220,365,40,40);

        g.setFont(new Font("楷体", Font.BOLD, 40));
        g.setColor(Color.BLACK);
        g.drawString(circleName,124,265);
        g.drawString(squareName,124,335);
        g.drawString("现在是   的回合",79,400);
        if (lastPiece!=null){
            g.drawString(str,545,400);
            //g.fillOval(21 + 135+getY(lastPiece)*60-25-60, 21 + 500+getX(lastPiece)*60-25-60,8,8);
        }

        //循环添加所有的行
        for (int a=0;a<15;a++){
            //循环添加一行里所有的列
            for (int b=0;b<15;b++){
                int piece = board[a][b];
                switch (piece){
                    case 1:
                        g.setColor(new Color(145, 120, 99, 255));
                        g.fillOval(135+b*60-25,500+a*60-25,50,50);
                        break;
                    case 2:
                        g.setColor(new Color(156, 192, 102, 255));
                        g.fillOval(135+b*60-25,500+a*60-25,50,50);
                        break;
                }
            }
        }

        g.dispose();
        return img;

    }


    /**
     * 结束判断算法。由于落子必是五连的组成部分，只需要判断落子是否构成五连即可判断获胜。
     *
     * @param xy 落子的坐标
     * @param board 棋盘数据
     */
    private boolean GobangOver(BigDecimal xy, int[][] board){
        //将xy坐标解析为x坐标及y坐标
        int x = getX(xy) -1;
        int y = getY(xy) -1;
        //p为落子类型（circle或square）
        int p = board[x][y];
        //先判断横向
        int total = 1;
        //向右，将连续的棋子数累计至total
        for (int yBy = y + 1; yBy >= 0 && yBy <= 14; yBy++ ){
            if (board[x][yBy] == p){
                total++;
            }else {
                break;
            }
        }
        //向左，将连续的棋子数累计至total
        for (int yBy = y -1; yBy >= 0 && yBy <= 14; yBy-- ){
            if (board[x][yBy] == p){
                total++;
            }else {
                break;
            }
        }
        //判断是否获胜，不是则重置total
        if (total >= 5){
            return true;
        }else {
            total = 1;
        }

        //纵向，先向上
        for (int xBy = x - 1; xBy >= 0 && xBy <= 14; xBy-- ){
            if (board[xBy][y] == p){
                total++;
            }else {
                break;
            }
        }
        //向下
        for (int xBy = x + 1; xBy >= 0 && xBy <= 14; xBy++ ){
            if (board[xBy][y] == p){
                total++;
            }else {
                break;
            }
        }
        //判断是否获胜，不是则重置total
        if (total >= 5){
            return true;
        }else {
            total = 1;
        }

        //斜向（右上至左下），先向右上。
        int yBy = y + 1;
        for (int xBy = x - 1 ;xBy>=0 && xBy<=14 && yBy>=0 && yBy<=14; xBy--  ){
            if (board[xBy][yBy] == p){
                total++;
                yBy++;
            }else {
                break;
            }
        }
        //向左下
        yBy = y - 1;
        for (int xBy = x + 1 ;xBy>=0 && xBy<=14 && yBy>=0 && yBy<=14; xBy++){
            if (board[xBy][yBy] == p){
                total++;
                yBy--;
            }else {
                break;
            }
        }
        //判断是否获胜，不是则重置total
        if (total >= 5){
            return true;
        }else {
            total = 1;
        }

        //斜向（左上至右下），先左上
        yBy = y - 1;
        for (int xBy = x - 1 ;xBy>=0 && xBy<=14 && yBy>=0 && yBy<=14; xBy--){
            if (board[xBy][yBy] == p){
                total++;
                yBy--;
            }else {
                break;
            }
        }
        //向右下
        yBy = y + 1;
        for (int xBy = x + 1 ;xBy>=0 && xBy<=14 && yBy>=0 && yBy<=14; xBy++){
            if (board[xBy][yBy] == p){
                total++;
                yBy++;
            }else {
                break;
            }
        }
        //判断是否获胜
        return total >= 5;
    }

    /**
     *判断坐标是否为有效坐标
     *
     * @param xy 输入的坐标
     */
    public boolean validXY(BigDecimal xy){
        int x = getX(xy);
        int y = getY(xy);
        return  (x>=1 && x<=15 && y>=1 && y<=15);
    }


    /**
     *使用正则判断str是否为数字
     */
    public static boolean isNumber(String str){
        String reg = "^[0-9]+(.[0-9]+)?$";
        return str.matches(reg);
    }


    /**
     * 从坐标中获取x
     */
    public Integer getX(BigDecimal xy){
        return xy.setScale(0,BigDecimal.ROUND_DOWN).intValue();
    }

    /**
     *从坐标中获取y
     */
    public Integer getY(BigDecimal xy){
        BigDecimal x = xy.setScale(0,BigDecimal.ROUND_DOWN);
        BigDecimal y = xy.subtract(x);
        String inStr = String.valueOf(y);
        int bitPos=inStr.indexOf(".");
        for (int numOfBits=inStr.length()-bitPos-1; numOfBits > 0; numOfBits--){
            y = y.multiply(BigDecimal.valueOf(10));
        }
        return y.intValue();
    }

    private void downloadOneFile(String fileName, String url) throws IOException {
        URL u = new URL(url);
        HttpURLConnection httpUrl = (HttpURLConnection) u.openConnection();
        httpUrl.connect();
        try (InputStream is = httpUrl.getInputStream(); FileOutputStream fs = new FileOutputStream(fileName)){
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fs.write(buffer, 0, len);
            }
        }
        httpUrl.disconnect();
    }


}
