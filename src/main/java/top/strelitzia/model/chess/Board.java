package top.strelitzia.model.chess;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 棋盘类，是整个象棋的入口
 */
public class Board {
    /**
     * 记录棋盘
     */
    private final Chess[][] board = new Chess[9][10];

    private Long p1;

    private  Long p2;

    //本次该红走棋
    public boolean next = true;

    //结构化棋谱，用于悔棋
    public final Stack<Move> chessStack = new Stack<>();

    //文字棋谱，记录
    public final Stack<String> chessManual = new Stack<>();

    public Board(Long p1, Long p2) {
        //初始化棋盘
        this.p1 = p1;
        this.p2 = p2;
        board[0][0] = new Car(true, 0, 0);
        board[1][0] = new Horse(true, 1, 0);
        board[2][0] = new Elephant(true, 2, 0);
        board[3][0] = new Defender(true, 3, 0);
        board[4][0] = new Marshal(true, 4, 0);
        board[5][0] = new Defender(true, 5, 0);
        board[6][0] = new Elephant(true, 6, 0);
        board[7][0] = new Horse(true, 7, 0);
        board[8][0] = new Car(true, 8, 0);

        board[1][2] = new Cannon(true, 1, 2);
        board[7][2] = new Cannon(true, 7, 2);

        board[0][3] = new Soldier(true, 0, 3);
        board[2][3] = new Soldier(true, 2, 3);
        board[4][3] = new Soldier(true, 4, 3);
        board[6][3] = new Soldier(true, 6, 3);
        board[8][3] = new Soldier(true, 8, 3);

        board[0][9] = new Car(false, 0, 9);
        board[1][9] = new Horse(false, 1, 9);
        board[2][9] = new Elephant(false, 2, 9);
        board[3][9] = new Defender(false, 3, 9);
        board[4][9] = new Marshal(false, 4, 9);
        board[5][9] = new Defender(false, 5, 9);
        board[6][9] = new Elephant(false, 6, 9);
        board[7][9] = new Horse(false, 7, 9);
        board[8][9] = new Car(false, 8, 9);

        board[1][7] = new Cannon(false, 1, 7);
        board[7][7] = new Cannon(false, 7, 7);

        board[0][6] = new Soldier(false, 0, 6);
        board[2][6] = new Soldier(false, 2, 6);
        board[4][6] = new Soldier(false, 4, 6);
        board[6][6] = new Soldier(false, 6, 6);
        board[8][6] = new Soldier(false, 8, 6);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int j = 9; j >= 0; j--) {
            sb.append(j).append(" ");
            for (int i = 0; i < 9; i++) {
                if (board[i][j] == null) {
                    sb.append("口");
                } else {
                    sb.append(board[i][j]);
                }
            }
            sb.append("\n");
        }
        sb.append(" 九八七六五四三二一");
        return sb.toString();
    }

    public BufferedImage toImg() throws IOException {
        BufferedImage bf = new BufferedImage(530, 580, BufferedImage.TYPE_INT_BGR);
        Graphics graphics = bf.getGraphics();
        graphics.drawImage(ImageIO.read(new File("runFile/chess/board.png")), 0, 0, null);
        if (next) {
            for (int j = 0; j <= 9; j++) {
                for (int i = 8; i >= 0; i--) {
                    if (board[i][j] != null) {
                        graphics.drawImage(ImageIO.read(new File(getChessPng(board[i][j]))), 460 - 55 * i, 515 - 55 * j,null);
                    }
                }
            }
            return bf;
        } else {
            for (int j = 0; j <= 9; j++) {
                for (int i = 8; i >= 0; i--) {
                    if (board[i][j] != null) {
                        graphics.drawImage(ImageIO.read(new File(getChessPng(board[i][j]))), 15 + 55 * i, 15 + 55 * j,null);
                    }
                }
            }
            return bf;
        }
    }

    public String getChessPng(Chess chess) {
        StringBuilder sb = new StringBuilder("runFile/chess/");
        if (chess.isRed) {
            sb.append("r_");
        } else {
            sb.append("b_");
        }

        sb.append(chess.getClass().getSimpleName().toLowerCase()).append(".png");
        return sb.toString();
    }

    public Info move(String order, Long user) {
        Move move = new Move();
        move.player = user;

        //移动棋子方法
        boolean isRed;

        //先判断是红棋还是黑棋
        if (user.equals(p1)) {
            isRed = true;
        } else if (user.equals(p2)) {
            isRed = false;
        } else {
            return new Info(false, "没这个玩家");
        }

        if (isRed != next) {
            return new Info(false, "还没轮到你");
        }

        //获取选择的那枚棋
        Info info = getChess(board, isRed, order.charAt(0), order.charAt(1));
        if (!info.b) {
            //如果选不到，就寄
            return info;
        }

        Chess chess = move.move = info.chess;

        Info chessInfo;
        switch (order.charAt(2)) {
            case '进':
                chessInfo = chess.forward(board, isRed, order.charAt(3), move);
                break;
            case '退':
                chessInfo = chess.back(board, isRed, order.charAt(3), move);
                break;
            case '平':
                chessInfo = chess.lateral(board, isRed, order.charAt(3), move);
                break;
            default:
                chessInfo = new Info(false, "命令无效");
                break;
        }

        if (chessInfo.b) {
            next = !isRed;
            chessManual.add(order);
            chessStack.add(move);
        }

        return chessInfo;
    }

    //悔棋方法
    public Info backOff(Chess[][] board, Long user) {
        Move pop = chessStack.pop();
        if (!pop.player.equals(user)) {
            return new Info(false, "没轮到你");
        }
        board[pop.startX][pop.startY] = pop.move;
        pop.eat.x = pop.startX;
        pop.eat.y = pop.startY;

        board[pop.endX][pop.endY] = pop.eat;
        chessManual.pop();
        return new Info(true, "悔棋成功");
    }


    public Info getChess(Chess[][] board, boolean isRed, char order1, char order2) {
        List<Character> characters = new ArrayList<Character>(){
            private static final long serialVersionUID = 5837432892626182324L;

            {
                add('一');
                add('二');
                add('三');
                add('四');
                add('五');
                add('六');
                add('七');
                add('八');
                add('九');
            }};
        if (order1 == '前' || order1 == '后' || order1 == '中' || ((order1 == '二' || order1 == '三' || order1 == '四') && order2 == '卒')) {
            //当命令为【前X】、【中X】、【后X】或【二卒】、【三卒】、【四卒】
            List<List<Chess>> boardList = new ArrayList<>();
            for (int i = 0; i < board.length; i++) {
                //遍历所有列，查看列里面的同路棋子有多少
                List<Chess> chessList = new ArrayList<>();
                for (int j = 0; j < board[0].length; j++) {
                    if (board[j][i] != null && board[j][i].name == order2 && isRed == board[j][i].isRed) {
                        chessList.add(board[j][i]);
                    }
                }
                //同一路有两个及以上棋子才行
                if (chessList.size() > 1) {
                    boardList.add(chessList);
                }
            }

            if (boardList.size() == 0) {
                return new Info(false, "没有同路的" + order2);
            }

            if (boardList.size() >= 2) {
                return new Info(false, "有多路同路的" + order2 + "请指定路数，如【四前平一】");
            }

            List<Chess> chessList = boardList.get(0);

            if (order1 == '前' && isRed || order1 == '后' && !isRed) {
                //因为数组是从底往上数的，因此红子的前和黑子的后，即为数组中的最后一个
                return new Info(true, chessList.get(chessList.size() - 1), "成功");
            } else if ((order1 == '二' || order1 == '中') && isRed) {
                //红子的二和中，即为数组中的倒数第二个
                if (chessList.size() > 2) {
                    return new Info(true, chessList.get(chessList.size() - 2), "成功");
                } else {
                    return new Info(false, "没有同路的第二颗子" + order2);
                }
            } else if (order1 == '二' || order1 == '中') {
                //黑子的二和中，即为数组中的第二个
                if (chessList.size() > 2) {
                    return new Info(true, chessList.get(1), "成功");
                } else {
                    return new Info(false, "没有同路的第二颗子" + order2);
                }
            } else if (order1 == '三' && isRed) {
                //红子的三，即为数组中的倒数第三个
                if (chessList.size() > 3) {
                    return new Info(true, chessList.get(chessList.size() - 3), "成功");
                } else {
                    return new Info(false, "没有同路的第三颗子" + order2);
                }
            } else if (order1 == '三') {
                //黑子的三，即为数组中的第三个
                if (chessList.size() > 3) {
                    return new Info(true, chessList.get(2), "成功");
                } else {
                    return new Info(false, "没有同路的第三颗子" + order2);
                }
            } else if (order1 == '四' && isRed) {
                //红子的四，即为数组中的倒数第四个
                if (chessList.size() > 4) {
                    return new Info(true, chessList.get(chessList.size() - 4), "成功");
                } else {
                    return new Info(false, "没有同路的第四颗子" + order2);
                }
            } else if (order1 == '四') {
                //黑子的四，即为数组中的第四个
                if (chessList.size() > 4) {
                    return new Info(true, chessList.get(3), "成功");
                } else {
                    return new Info(false, "没有同路的第四颗子" + order2);
                }
            }else {
                //红子的后和黑子的前，即为数组中的第一个
                return new Info(true, chessList.get(0), "成功");
            }
        } else if ((order2 == '前' || order2 == '后' || order2 == '中') && characters.contains(order1)) {
            //判断【X前】、【X中】、【X后】这种
            List<Chess> chessList = new ArrayList<>();
            for (int i = 0; i < board.length; i++) {
                int xi;
                if (isRed) {
                    xi = 9 - Board.getIntByCh(order1);
                } else {
                    xi = Board.getIntByCh(order1) - 1;
                }
                Chess chess = board[xi][i];
                if (chess != null && chess.isRed == isRed && (chess.name == '兵' || chess.name == '卒')) {
                    chessList.add(chess);
                }
            }
            if (order2 == '后' && isRed || order2 == '前' && isRed) {
                if (chessList.size() > 1) {
                    return new Info(true, chessList.get(0), "成功");
                } else {
                    return new Info(false, "指定列无同路棋子");
                }
            } else if (order2 == '中') {
                if (chessList.size() > 2) {
                    return new Info(true, chessList.get(1), "成功");
                } else {
                    return new Info(false, "指定列无同路棋子");
                }
            } else {
                if (chessList.size() > 1) {
                    return new Info(true, chessList.get(chessList.size() - 1), "成功");
                } else {
                    return new Info(false, "指定列无同路棋子");
                }
            }
        } else {
            List<Chess> chessList = new ArrayList<>();
            //普通的选子，比如"车五"
            int x = getIntByCh(order2) - 1;
            if (isRed) {
                x = 8 - x;
            }
            for (int i = 0; i < board[0].length; i++) {
                if (board[x][i] != null && board[x][i].name == order1 && isRed == board[x][i].isRed) {
                    chessList.add(board[x][i]);
                }
            }
            if (chessList.size() > 1) {
                return new Info(false, "可选的太多了");
            }

            if (chessList.size() == 0) {
                return new Info(false, "没这颗子");
            }

            return new Info(true, chessList.get(0), "成功");
        }
    }

    public static int getIntByCh(char order) {
        //汉字转int
        int x = 0;
        switch (order) {
            case '一':
                x = 1;
                break;
            case '二':
                x = 2;
                break;
            case '三':
                x = 3;
                break;
            case '四':
                x = 4;
                break;
            case '五':
                x = 5;
                break;
            case '六':
                x = 6;
                break;
            case '七':
                x = 7;
                break;
            case '八':
                x = 8;
                break;
            case '九':
                x = 9;
                break;
        }
        return x;
    }

    public Chess[][] getBoard() {
        return board;
    }

    public Long getP1() {
        return p1;
    }

    public void setP1(Long p1) {
        this.p1 = p1;
    }

    public Long getP2() {
        return p2;
    }

    public void setP2(Long p2) {
        this.p2 = p2;
    }

    public boolean isRedNext() {
        return next;
    }

    public void setNext(boolean next) {
        this.next = next;
    }

    public Stack<Move> getChessStack() {
        return chessStack;
    }

    public Stack<String> getChessManual() {
        return chessManual;
    }
}