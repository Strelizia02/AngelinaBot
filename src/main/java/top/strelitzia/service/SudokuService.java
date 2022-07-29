package top.strelitzia.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import top.angelinaBot.annotation.AngelinaGroup;
import top.angelinaBot.container.AngelinaEventSource;
import top.angelinaBot.container.AngelinaListener;
import top.angelinaBot.model.MessageInfo;
import top.angelinaBot.model.ReplayInfo;
import top.angelinaBot.model.TextLine;
import top.angelinaBot.util.SendMessageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SudokuService {

    private static final Set<Long> groupList = new HashSet<>();

    @Autowired
    SendMessageUtil sendMessageUtil;


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SudokuService.class);

    @AngelinaGroup(keyWords = {"test"} )
    public ReplayInfo sudokuTest(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        int[][] filled = new int[9][9];
        SudokuGenerator s = new SudokuGenerator(9);
        int[][] puzzle = s.generatePuzzle(generateAnswer(), Integer.parseInt(messageInfo.getArgs().get(1)),50);
        replayInfo.setReplayImg(drawBoard(puzzle,filled, Integer.parseInt(messageInfo.getArgs().get(1))));
        return replayInfo;
    }

    @AngelinaGroup(keyWords = {"数独"} )
    public ReplayInfo sudoku(MessageInfo messageInfo) {
        ReplayInfo replayInfo = new ReplayInfo(messageInfo);
        Long groupId = messageInfo.getGroupId();
        if (groupList.contains(groupId)) {
            replayInfo.setReplayMessage("本群正在进行数独哦~可以试试发送“查看数独“");
            return replayInfo;
        }
        groupList.add(messageInfo.getGroupId());
        int[][] answer;
        int[][] puzzle;
        int[][] filled = new int[9][9];
        int difficulty = 1;
        String path = "runFile/sudoku/"+messageInfo.getGroupId();
        File file =new File(path);
        //如果文件夹不存在则创建
        if (!file.exists() && !file.isDirectory()) file.mkdirs();
        file = new File(path + "/filled.txt");
        if (file.exists()){
            //询问是否继续
            puzzle = readTxt(path+"/puzzle.txt");
            difficulty = getDifficulty(puzzle);
            String string = null;
            switch (difficulty){
                case 0:
                    string = "（简单）";
                    break;
                case 1:
                    string = "（普通）";
                    break;
                case 2:
                    string = "（困难）";
                    break;
            }
            replayInfo.setReplayMessage("本群还有尚未完成的数独哦~" + string + "\n请问要继续嘛？\n请回答 继续 或 重开");
            sendMessageUtil.sendGroupMsg(replayInfo);
            replayInfo.setReplayMessage(null);

            AngelinaListener angelinaListener = new AngelinaListener() {
                @Override
                public boolean callback(MessageInfo message) {
                    return message.getGroupId().equals(messageInfo.getGroupId()) &&
                            message.getQq().equals(messageInfo.getQq()) &&
                            (message.getText().equals("继续") || message.getText().equals("重开"));
                }
            };
            angelinaListener.setGroupId(messageInfo.getGroupId());
            MessageInfo recall1 = AngelinaEventSource.waiter(angelinaListener).getMessageInfo();

            if (recall1.getText().equals("重开")){
                replayInfo.setReplayMessage("请选择难度：\n简单（42空）\n普通（50空）\n困难（58空）");
                sendMessageUtil.sendGroupMsg(replayInfo);
                replayInfo.setReplayMessage(null);

                angelinaListener = new AngelinaListener() {
                    @Override
                    public boolean callback(MessageInfo message) {
                        return message.getGroupId().equals(messageInfo.getGroupId()) &&
                                message.getQq().equals(messageInfo.getQq()) &&
                                (message.getText().equals("简单") || message.getText().equals("普通") || message.getText().equals("困难"));
                    }
                };
                angelinaListener.setGroupId(messageInfo.getGroupId());
                MessageInfo recall2 = AngelinaEventSource.waiter(angelinaListener).getMessageInfo();

                if (recall2 == null) {
                    groupList.remove(messageInfo.getGroupId());
                    return null;
                }

                switch (recall2.getText()){
                    case "简单":
                        difficulty = 0;
                        break;
                    case "普通":
                        difficulty = 1;
                        break;
                    case "困难":
                        difficulty = 2;
                        break;
                }
                answer = generateAnswer();
                writeTxt(answer,path+"/answer.txt");
                SudokuGenerator s = new SudokuGenerator(9);
                puzzle = s.generatePuzzle(answer, difficulty,50);
                writeTxt(puzzle,path+"/puzzle.txt");
                writeTxt(filled,path+"/filled.txt");

                //继续的话其实没什么操作需要做了，硬写写一个
            }else if (recall1.getText().equals("继续")){
                replayInfo.setReplayMessage("稀音正在为您重连中......");
                sendMessageUtil.sendGroupMsg(replayInfo);
                replayInfo.setReplayMessage(null);
            }

        }else {
            replayInfo.setReplayMessage("请选择难度：\n简单（42空）\n普通（50空）\n困难（58空左右）");
            sendMessageUtil.sendGroupMsg(replayInfo);
            replayInfo.setReplayMessage(null);

            AngelinaListener angelinaListener = new AngelinaListener() {
                @Override
                public boolean callback(MessageInfo message) {
                    return message.getGroupId().equals(messageInfo.getGroupId()) &&
                            message.getQq().equals(messageInfo.getQq()) &&
                            (message.getText().equals("简单") || message.getText().equals("普通") || message.getText().equals("困难"));
                }
            };
            angelinaListener.setGroupId(messageInfo.getGroupId());
            MessageInfo recall2 = AngelinaEventSource.waiter(angelinaListener).getMessageInfo();

            if (recall2 == null) {
                groupList.remove(messageInfo.getGroupId());
                return null;
            }

            switch (recall2.getText()){
                case "简单":
                    difficulty = 0;
                    break;
                case "普通":
                    difficulty = 1;
                    break;
                case "困难":
                    difficulty = 2;
                    break;
            }
            answer = generateAnswer();
            writeTxt(answer,path+"/answer.txt");
            SudokuGenerator s = new SudokuGenerator(9);
            puzzle = s.generatePuzzle(answer, difficulty,50);
            writeTxt(puzzle,path+"/puzzle.txt");
            writeTxt(filled,path+"/filled.txt");
        }



        answer = readTxt(path+"/answer.txt");
        puzzle = readTxt(path+"/puzzle.txt");
        filled = readTxt(path+"/filled.txt");
        replayInfo.setReplayMessage("指令示例（第1行第2列填3）：\n1.23\n1.2填3\n1.2,3\n甚至阿巴1阿巴.阿巴2阿巴3");
        sendMessageUtil.sendGroupMsg(replayInfo);
        replayInfo.setReplayMessage(null);
        replayInfo.setReplayImg(drawBoard(puzzle, filled, difficulty));
        sendMessageUtil.sendGroupMsg(replayInfo);
        replayInfo.getReplayImg().clear();


        Map<String, Integer> score = new HashMap<>();
        List<Long> overVote = new ArrayList<>();
        List<Long> pauseVote = new ArrayList<>();
        int time = 0;
        //开始循环处理回答
        for (int i=0;i<81;i++){
            AngelinaListener angelinaListener = new AngelinaListener() {
                @Override
                public boolean callback(MessageInfo message) {
                    return message.getGroupId().equals(messageInfo.getGroupId()) &&
                            (validRecall(message.getText()) || message.getText().equals("查看数独") ||
                                    message.getText().equals("结束数独") || message.getText().equals("查看答案"));
                }
            };
            angelinaListener.setGroupId(messageInfo.getGroupId());
            MessageInfo recall = AngelinaEventSource.waiter(angelinaListener).getMessageInfo();

            if (recall == null) {
                time++;
                if (time==10) {
                    groupList.remove(messageInfo.getGroupId());
                    return null;
                }
                i--;
                continue;
            }

            if (recall.getText().equals("查看数独")){
                replayInfo.setReplayImg(drawBoard(puzzle, filled, difficulty));
                sendMessageUtil.sendGroupMsg(replayInfo);
                replayInfo.getReplayImg().clear();
                i--;
                continue;
            }

            if (recall.getText().equals("结束数独")){
                if (pauseVote.contains(recall.getQq())){
                    replayInfo.setReplayMessage("您已经参与过“结束数独”的投票啦");
                    sendMessageUtil.sendGroupMsg(replayInfo);
                    replayInfo.setReplayMessage(null);
                    i--;
                    continue;
                }
                pauseVote.add(recall.getQq());
                replayInfo.setReplayMessage("“结束数独”投票（" + pauseVote.size() +"/3）");
                sendMessageUtil.sendGroupMsg(replayInfo);
                replayInfo.setReplayMessage(null);
                if (pauseVote.size()==3){
                    replayInfo.setReplayMessage("数独已结束，下次开启时可以选择继续未完成的数独哦");
                    TextLine textLine = new TextLine();
                    textLine.addString("数独结束，来看得分榜吧:");
                    textLine.nextLine();
                    LinkedHashMap<String,Integer> finalScore = finalScore(score);
                    for (String name: finalScore.keySet()) {
                        textLine.addString(name + "填对了" + finalScore.get(name) + "个空");
                        textLine.nextLine();
                    }
                    replayInfo.setReplayImg(textLine.drawImage());
                    groupList.remove(messageInfo.getGroupId());
                    return replayInfo;
                }
                i--;
                continue;
            }


            if (recall.getText().equals("查看答案")){
                if (overVote.contains(recall.getQq())){
                    replayInfo.setReplayMessage("您已经参与过“查看答案”的投票啦");
                    sendMessageUtil.sendGroupMsg(replayInfo);
                    replayInfo.setReplayMessage(null);
                    i--;
                    continue;
                }
                overVote.add(recall.getQq());
                replayInfo.setReplayMessage("“查看答案”投票（" + overVote.size() +"/3）\n此操作会使数独强制结束，且下次无法继续本局");
                sendMessageUtil.sendGroupMsg(replayInfo);
                replayInfo.setReplayMessage(null);
                if (overVote.size()==3){
                    filled = new int[9][9];
                    replayInfo.setReplayImg(drawBoard(answer, filled, difficulty));
                    sendMessageUtil.sendGroupMsg(replayInfo);
                    replayInfo.getReplayImg().clear();
                    replayInfo.setReplayMessage("数独已结束，");
                    TextLine textLine = new TextLine();
                    textLine.addString("数独结束，来看得分榜吧:");
                    textLine.nextLine();
                    LinkedHashMap<String,Integer> finalScore = finalScore(score);
                    for (String name: finalScore.keySet()) {
                        textLine.addString(name + "填对了" + finalScore.get(name) + "个空");
                        textLine.nextLine();
                    }
                    replayInfo.setReplayImg(textLine.drawImage());
                    file = new File(path+"/filled.txt");
                    file.delete();
                    groupList.remove(messageInfo.getGroupId());
                    return replayInfo;
                }
                i--;
                continue;
            }


            int num = recall.getText().charAt(recall.getText().length()-1)-'0';
            String str = recall.getText();
            str = str.substring(0, str.length() - 1);//去掉最后一位
            //去掉无关信息，只保留数字和小数点
            String REGEX ="[^(0-9).]";
            str = Pattern.compile(REGEX).matcher(str).replaceAll("").trim();

            BigDecimal xy = new BigDecimal(str);
            int x = getX(xy) -1;
            int y = getY(xy) -1;


            if (puzzle[x][y]==0 && filled[x][y]==0){
                if (num == answer[x][y]){
                    replayInfo.setReplayMessage(recall.getName() + "，答对啦！");
                    sendMessageUtil.sendGroupMsg(replayInfo);
                    replayInfo.setReplayMessage(null);
                    //记录答对的人
                    if (score.containsKey(recall.getName())) {
                        score.put(recall.getName(), score.get(recall.getName()) + 1);
                    } else {
                        score.put(recall.getName(), 1);
                    }
                    //更新filled信息，并发送填完后的数独
                    filled[x][y] = num;
                    writeTxt(filled,path+"/filled.txt");
                    if (!over(puzzle,filled)) {
                        replayInfo.setReplayImg(drawBoard(puzzle, filled, difficulty));
                        sendMessageUtil.sendGroupMsg(replayInfo);
                        replayInfo.getReplayImg().clear();
                        continue;
                    }else {
                        groupList.remove(messageInfo.getGroupId());
                        file = new File(path+"/filled.txt");
                        file.delete();
                        TextLine textLine = new TextLine();
                        textLine.addString("数独填完啦！来看得分榜吧:");
                        textLine.nextLine();
                        LinkedHashMap<String,Integer> finalScore = finalScore(score);
                        for (String name: finalScore.keySet()) {
                            textLine.addString(name + "填对了" + finalScore.get(name) + "个空");
                            textLine.nextLine();
                        }
                        replayInfo.setReplayImg(textLine.drawImage());
                        return replayInfo;
                    }
                }else {
                    replayInfo.setReplayMessage(recall.getName() + "，答错啦qwq，再想想吧！");
                    sendMessageUtil.sendGroupMsg(replayInfo);
                    replayInfo.setReplayMessage(null);
                    i--;
                    continue;
                }
            }else if (puzzle[x][y] != 0){
                replayInfo.setReplayMessage(recall.getName() + "，这里不能填哦");
                sendMessageUtil.sendGroupMsg(replayInfo);
                replayInfo.setReplayMessage(null);
                i--;
                continue;
            } else if (filled[x][y] != 0) {
                replayInfo.setReplayMessage(recall.getName() + "，这里已经填好啦");
                sendMessageUtil.sendGroupMsg(replayInfo);
                replayInfo.setReplayMessage(null);
                i--;
                continue;
            }
        }
        return replayInfo;
    }

    public static void main(String[] args) {
        String str = "1.14";
        int num = str.charAt(str.length()-1)-'0';
        str = str.substring(0, str.length() - 1);//去掉最后一位
        //去掉无关信息，只保留数字和小数点
        String REGEX ="[^(0-9).]";
        str = Pattern.compile(REGEX).matcher(str).replaceAll("").trim();

        BigDecimal xy = new BigDecimal(str);
        int x = getX(xy);
        int y = getY(xy);
    }

    private boolean over(int[][] puzzle,int[][] filled){
        int zero = 0;
        for(int r=0;r<9;r++){
            for (int c=0;c<9;c++){
                if (puzzle[r][c]==0) zero+=1;
                if (filled[r][c]==0) zero+=1;
            }
        }
        return zero==81;
    }


    //整理map，按value排序
    public LinkedHashMap<String,Integer> finalScore(Map<String,Integer> map){
        List<Map.Entry<String,Integer>> lstEntry = new ArrayList<>(map.entrySet());
        Collections.sort(lstEntry,((o1, o2) -> {
            return o2.getValue().compareTo(o1.getValue());
        }));
        /*lstEntry.forEach(o->{
            System.out.println(o.getKey()+":"+o.getValue());
        });*/

        //如果一定要返回一个map，就new一个LinkedHashMap，将list中所有值依次put进去就可以
    LinkedHashMap<String,Integer> linkedHashMap=new LinkedHashMap<>();
    lstEntry.forEach(o->{
        linkedHashMap.put(o.getKey(),o.getValue());
    });
        return linkedHashMap;
    }

    //判断string是否符合指令格式，末位为非零数字，且前面包含小数
    public boolean validRecall(String recall){
        //recall的最后一位是否为非零数字
        if (!isNumber(String.valueOf(recall.charAt(recall.length()-1))) || recall.charAt(recall.length()-1)-'0'==0)
            return false;
        recall = recall.substring(0, recall.length() - 1);//去掉最后一位
        //去掉无关信息，只保留数字和小数点
        String REGEX ="[^(0-9).]";
        recall = Pattern.compile(REGEX).matcher(recall).replaceAll("").trim();
        //判断横纵坐标是否均在范围内
        BigDecimal xy;
        try{
            xy = new BigDecimal(recall);
        }catch (NumberFormatException e){
            return false;
        }
        int x = getX(xy);
        int y = getY(xy);
        return (x>=1 && x<=9 && y>=1 && y<=9);
    }

    public static boolean isNumber(String str){
        String reg = "^[0-9]+(.[0-9]+)?$";
        return str.matches(reg);
    }

    //从坐标中获取x
    public static Integer getX(BigDecimal xy){
        return xy.setScale(0,BigDecimal.ROUND_DOWN).intValue();
    }

    //从坐标中获取y
    public static Integer getY(BigDecimal xy){
        BigDecimal x = xy.setScale(0,BigDecimal.ROUND_DOWN);
        BigDecimal y = xy.subtract(x);
        String inStr = String.valueOf(y);
        inStr = inStr.substring(2);
        return Integer.valueOf(inStr);
    }

    public static String boardToString(int[][] board){
        StringBuilder str = new StringBuilder();
        for(int r=0;r<9;r++){
            for (int c=0;c<9;c++){
                str.append(board[r][c]);
            }
            str.append("\n");
        }
        return String.valueOf(str);
    }


    public static void writeTxt(int[][] board,String path){
        String word = boardToString(board);
        FileOutputStream fileOutputStream = null;
        File file = new File(path);
        try {
        if(!file.exists()){
            file.createNewFile();
        }
        fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(word.getBytes(StandardCharsets.UTF_8));
        fileOutputStream.flush();
        fileOutputStream.close();
        } catch (IOException e) {
            log.warn("写入txt错误");
            throw new RuntimeException(e);
        }
    }



    public static int[][] readTxt(String path) {
        File file = new File(path);
        if(file.isFile() && file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer sb = new StringBuffer();
                String text = null;
                while((text = bufferedReader.readLine()) != null){
                    sb.append(text);
                }
                String str = sb.toString();
                //获取完str，循环存入数组
                int[][] board = new int[9][9];
                int i = 0;
                for(int r=0;r<9;r++){
                    for (int c=0;c<9;c++){
                        board[r][c] = Integer.parseInt(str.charAt(i)+"");
                        i++;
                    }
                }
                return board;
            } catch (Exception e) {
                log.warn("读取txt错误");
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getDifficulty(int[][] puzzle){
        int zero = 0;
        int difficulty = -1;
        for(int r=0;r<9;r++){
            for (int c=0;c<9;c++){
                if (puzzle[r][c]==0) zero+=1;
            }
        }
        switch (zero){
            case 42:
                difficulty = 0;
                break;
            case  50:
                difficulty = 1;
                break;
            default:
                difficulty = 2;
                break;
        }
        return difficulty;
    }

    public BufferedImage drawBoard(int[][] puzzle,int[][] filled,int difficulty){
        BufferedImage img = new BufferedImage(800,900,BufferedImage.TYPE_INT_BGR);
        Graphics g = img.getGraphics();
        //填充背景
        g.setColor(Color.WHITE);
        g.fillRect(0,0,800,900);
        //设置字体
        g.setFont(new Font("宋体", Font.BOLD, 60));
        g.setColor(Color.black);
        StringBuilder s = new StringBuilder("数独");
        switch (difficulty){
            case 0:
                s.append("（Easy）");
                break;
            case 1:
                s.append("（Medium）");
                break;
            case 2:
                s.append("（Hard）");
                break;

        }
        g.drawString(String.valueOf(s),200,100);

        //划细线
        g.fillRect(40,219,720,2);
        g.fillRect(40,299,720,2);
        g.fillRect(40,459,720,2);
        g.fillRect(40,539,720,2);
        g.fillRect(40,699,720,2);
        g.fillRect(40,779,720,2);

        g.fillRect(119,140,2,720);
        g.fillRect(199,140,2,720);
        g.fillRect(359,140,2,720);
        g.fillRect(439,140,2,720);
        g.fillRect(599,140,2,720);
        g.fillRect(679,140,2,720);

        //划粗线
        g.fillRect(40,138,720,4);
        g.fillRect(40,378,720,4);
        g.fillRect(40,618,720,4);
        g.fillRect(40,858,720,4);

        g.fillRect( 38,140,4,720);
        g.fillRect(278,140,4,720);
        g.fillRect(518,140,4,720);
        g.fillRect(758,140,4,720);

        //循环添加数字
        g.setFont(new Font("AR PL UMing HK", Font.BOLD, 80));
        for(int r=0;r<9;r++){
            for (int c=0;c<9;c++){
                if (puzzle[r][c] !=0) g.drawString(String.valueOf(puzzle[r][c]),18 + 40 + c*80,-10 + 140 + (r+1)*80);
            }
        }
        g.setColor(new Color(54, 95, 178));
        for(int r=0;r<9;r++){
            for (int c=0;c<9;c++){
                if (filled[r][c] !=0) g.drawString(String.valueOf(filled[r][c]),18 + 40 + c*80,-10 +140 + (r+1)*80);
            }
        }
        //获取logo
        try {
            InputStream is = new ClassPathResource("/pic/logo.jpg").getInputStream();
            g.drawImage(ImageIO.read(is), 780, 0, 20 , 20 , null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        g.dispose();

        return img;
    }


    public static int[][] generateAnswer() {
        int k1, k2, counter = 1;
        SudokuBoard bd = new SudokuBoard();
        bd.generate();
        bd.getBoard();
        // swap singles
        bd.gen_rand(1); // rows
        bd.gen_rand(0); // cols

        // swap groups
        Random rand=new Random();
        int n[] = { 0, 3, 6 };
        for(int i = 0; i < 2; i++) {
            k1 = n[rand.nextInt(n.length)];

            do{
                k2 = n[rand.nextInt(n.length)];
            }while(k1 == k2);

            if(counter == 1) bd.swap_row_group(k1, k2);
            else bd.swap_col_group(k1, k2);

            counter++;
        }
        return bd.getBoard();
    }
}



/**
 * 地址https://github.com/AndreasHaaversen/SudokuGenerator
 * 只拿了挖洞算法，生成好像有点问题
 */
class SudokuGenerator extends SudokuSolver {

    private static final int EASY = 0;
    private static final int MEDIUM = 1;
    private static final int HARD = 2;
    private static final int DEFAULT_PATIENCE = 50;

    private List<Tuple<Integer, Integer>> positions = new ArrayList<Tuple<Integer, Integer>>();

    public SudokuGenerator(int size) {
        super(size);
    }

    public int[][] generatePuzzle(int[][] answer,int difficulty, int patience){
        board = answer;
        int holes = getNumHoles(difficulty);
        getPositions(size);
        makeHoles(holes, patience);
        return board;
    }

    private void getPositions(int size) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                positions.add(new Tuple<Integer, Integer>(i, j));
            }
        }
    }

    private boolean makeHoles(int difficulty, int patience) {
        Random rand = new Random();
        int removed = 0;
        int lastRemoved = 0;
        int tries = 0;
        while (removed < difficulty) {
            if (lastRemoved == removed) {
                tries++;
            }
            if (tries > patience || positions.isEmpty()) {
                return false;
            }

            lastRemoved = removed;
            Tuple<Integer, Integer> candidate = positions.remove(rand.nextInt(positions.size()));
            int x = candidate.x;
            int y = candidate.y;
            if (x != y && board[x][y] != 0 && board[y][x] != 0) {
                if (removeAndTestPair(x, y, y, x)) {
                    positions.removeIf(p -> p.x == y && p.y == x);
                    removed += 2;
                }
            } else if (board[x][y] != 0) {
                if (removeAndTest(x, y)) {
                    removed += 1;
                }
            } else if (board[y][x] != 0) {
                if (removeAndTest(x, y)) {
                    removed += 1;
                }
            }
            //System.out.println("Removed: " + removed);
        }
        return true;
    }

    public boolean removeAndTestPair(int x1, int y1, int x2, int y2) {
        int tmp1 = board[x1][y1];
        int tmp2 = board[x2][y2];
        board[x1][y1] = 0;
        board[x2][y2] = 0;
        super.solve(board);
        if (super.num_solutions == 1) {
            return true;
        } else {
            board[x1][y1] = tmp1;
            board[x2][y2] = tmp2;
            return false;
        }
    }

    public boolean removeAndTest(int x, int y) {
        int tmp = board[x][y];
        board[x][y] = 0;
        super.solve(board);
        if (super.num_solutions == 1) {
            return true;
        } else {
            board[x][y] = tmp;
            return false;
        }
    }

    private int getNumHoles(int difficulty) {
        int out = 0;
        if (size == 2) {
            switch(difficulty) {
                case(EASY): out = 1; break;
                case(MEDIUM): out = 2; break;
                case(HARD): out = 3; break;
                default: out = 0; break;
            }
        } else if(size == 4) {
            switch(difficulty) {
                case(EASY): out = 5; break;
                case(MEDIUM): out = 7; break;
                case(HARD): out = 9; break;
                default: out = 0; break;
            }
        } else if(size == 9) {
            switch(difficulty) {
                case(EASY): out = 42; break;
                case(MEDIUM): out = 50; break;
                case(HARD): out = 58; break;
                default: out = 0; break;
            }
        } else if(size == 16) {
            switch(difficulty) {
                case(EASY): out = 88; break;
                case(MEDIUM): out = 108; break;
                case(HARD): out = 130; break;
                default: out = 0; break;
            }
        }
        return out;
    }

}


class SudokuSolver {

    public int[][] board;
    protected final int size;
    protected final int root;
    protected ArrayList<Integer> allowed = new ArrayList<Integer>();
    public int num_solutions;

    public SudokuSolver(int size) {
        this.board = new int[size][size];
        this.size = size;
        this.root = (int) Math.floor(Math.sqrt(size));
        this.num_solutions = 0;
        for (int i = 1; i <= size; i++) {
            allowed.add(i);
        }
    }

    public SudokuSolver(int[][] board) {
        this.board = board;
        this.size = board.length;
        this.root = (int) Math.floor(Math.sqrt(size));
        for (int i = 1; i <= size; i++) {
            allowed.add(i);
        }
    }

    public int[][] solve(int[][] board) {
        num_solutions = 0;
        return solveSudoku(board);
    }

    public int[][] solveSudoku(int[][] board) {
        int last = 1;
        int j = 0;
        int i = 0;

        while(true) {
            if(board[i][j] == 0) {
                while(last <= board.length) {
                    if(isSafe(i,j,last)) {
                        board[i][j] = last;
                        solveSudoku(board);
                    }
                    last++;
                }
                board[i][j] = 0;
                return board;
            } else if(i < board.length-1) {
                i++;
            } else if( i == board.length-1 && j < board.length-1) {
                i = 0;
                j++;
            } else {
                num_solutions++;
                return board;
            }
        }
    }

    protected boolean isSafe(int row, int col, int n) {
        return (safeRow(row, n) && safeCol(col, n) && safeBox(row, col, n));
    }

    private boolean safeRow(int row, int n) {
        for (int i = 0; i < this.size; i++) {
            if (board[row][i] == n) {
                return false;
            }
        }
        return true;
    }

    private boolean safeCol(int col, int n) {
        for (int i = 0; i < this.size; i++) {
            if (board[i][col] == n) {
                return false;
            }
        }
        return true;
    }

    private boolean safeBox(int row, int col, int n) {
        int r = row - row % root;
        int c = col - col % root;
        for (int i = r; i < r + root; i++) {
            for (int j = c; j < c + root; j++) {
                if (board[i][j] == n) {
                    return false;
                }
            }
        }
        return true;
    }

    private String encodeBoard() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                out.append(board[i][j] + ";");
            }
        }
        return out.toString();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int val = board[i][j];
                if (val == 0) {
                    out.append("0");
                } else {
                    out.append(String.valueOf(val));
                }
                // out.append(';');
            }
        }
        return out.toString();
    }
}

class Tuple<X, Y> {
    public final X x;
    public final Y y;
    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Tuple)){
            return false;
        }

        Tuple<X,Y> other_ = (Tuple<X,Y>) other;

        // this may cause NPE if nulls are valid values for x or y. The logic may be improved to handle nulls properly, if needed.
        return other_.x.equals(this.x) && other_.y.equals(this.y);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((x == null) ? 0 : x.hashCode());
        result = prime * result + ((y == null) ? 0 : y.hashCode());
        return result;
    }
}



/**
 * 生成数独用了这个，通过对母数独随机交换行列来生成随机数独
 * Created by scott on 2016-07-08.
 */
class SudokuBoard {
    public static int [][] board = new int[9][9];

    public static void generate() {
        int a, b = 1;
        for(int i = 0; i < 9; i++) {
            a = b;
            for(int j = 0; j < 9; j++) {
                if(a<=9) {
                    board[i][j] = a;
                    a++;
                }
                else {
                    a = 1;
                    board[i][j] = a;
                    a++;
                }
            } //fill row

            b = a + 3;
            // keeps validity aisdjasid
            if(a == 10) b = 4;
            if(b > 9) b = (b%9) + 1;
        }
    }

    public static void gen_rand(int check) {
        int s1, s2, max = 2, min = 0;
        Random r = new Random();

        for(int i = 0; i < 3; i++) {
            s1 = r.nextInt(max - min + 1) + min;

            // ensure different row/cols are selected to swap
            do {
                s2 = r.nextInt(max - min + 1) + min;
            } while(s1==s2);

            max += 3;
            min += 3;

            if(check == 1) swap_rows(s1,s2);
            else if(check == 0) swap_cols(s1,s2);
        }
    }

    // swap single lines between group sections
    public static void swap_rows(int row1, int row2) {
        int temp;

        for(int a = 0; a < 9; a++) {
            temp = board[row1][a];
            board[row1][a] = board[row2][a];
            board[row2][a] = temp;
        }
    }

    public static void swap_cols(int col1, int col2) {
        int temp;

        for(int a = 0; a < 9; a++) {
            temp = board[a][col1];
            board[a][col1] = board[a][col2];
            board[a][col2] = temp;
        }
    }

    // swap groups of 3
    public static void swap_row_group(int rows1, int rows2) {
        int temp;

        for(int j = 1; j <= 3; j++) {
            for(int k = 0; k < 9; k++) {
                temp = board[rows1][k];
                board[rows1][k] = board[rows2][k];
                board[rows2][k] = temp;
            }
            rows1++;
            rows2++;
        }
    }

    public static void swap_col_group(int cols1, int cols2) {
        int temp;

        for(int j = 1; j <= 3; j++) {
            for(int k = 0; k < 9; k++) {
                temp = board[k][cols1];
                board[k][cols1] = board[k][cols2];
                board[k][cols2] = temp;
            }
            cols1++;
            cols2++;
        }
    }

    public int[][] getBoard() {
        return board;
    }
}