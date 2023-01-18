package top.strelitzia.model.minesweeping;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

public class MineSweeping {
    //共有8个方向
    int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, 1}, {1, -1}, {-1, -1}};
    //地图，左上角是0,0
    Element[][] board;
    //记录雷的数量
    int sweepNum;
    int num;

    /**
     * 构造方法，level可取1,2,3分别为低级、中级、高级，默认为低级
     * @param level 难度等级
     */
    public MineSweeping(int level) {
        //初始化方法，有高中低三个级别
        switch (level) {
            case 2:
                board = new Element[16][16];
                sweepNum = 40;
                num = 16 * 16 - 40;
                break;
            case 3:
                board = new Element[16][30];
                num = 16 * 30 - 99;
                sweepNum = 99;
                break;
            default:
                board = new Element[9][9];
                num = 9 * 9 - 10;
                sweepNum = 10;
                break;
        }
        //根据初始化地图进行随机放置地雷/数字
        createBoard();
    }

    public MineSweeping() {
        this(1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board[0].length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j].isOpen) {
                    sb.append(" ").append(board[i][j].getNumber());
                } else if (board[i][j].isFlag()){
                    sb.append(" a");
                } else {
                    sb.append(" x");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String toString(Boolean b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board[0].length; i++) {
            for (int j = 0; j < board.length; j++) {
                sb.append(" ").append(board[i][j].getNumber());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public BufferedImage toImg() throws IOException {
        BufferedImage bf = new BufferedImage(16 * board[0].length + 16, 16 * board.length + 16, BufferedImage.TYPE_INT_BGR);
        Graphics graphics = bf.getGraphics();
        for (int i = 0; i < board[0].length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j].isOpen) {
                    graphics.drawImage(ImageIO.read(new File("runFile/minesweeping/" + board[i][j].getNumber() + ".bmp")), 16 * i + 16, 16 * j + 16,null);
                } else if (board[i][j].isFlag()){
                    graphics.drawImage(ImageIO.read(new File("runFile/minesweeping/13.bmp")), 16 * i + 16, 16 * j + 16, null);
                } else {
                    graphics.drawImage(ImageIO.read(new File("runFile/minesweeping/9.bmp")), 16 * i + 16, 16 * j + 16, null);
                }
            }
        }

        for (int i = 0; i < board[0].length; i++) {
            graphics.drawString(String.valueOf(i), 21 + 16 * i, 16);
        }

        for (int j = 0; j < board.length; j++) {
            graphics.drawString(String.valueOf(j), 5, 16 * j + 30);
        }
        return bf;
    }

    public BufferedImage toImgOver() throws IOException {
        BufferedImage bf = new BufferedImage(16 * board[0].length + 16, 16 * board.length + 16, BufferedImage.TYPE_INT_BGR);
        Graphics graphics = bf.getGraphics();
        for (int i = 0; i < board[0].length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j].number != 9) {
                    if (board[i][j].isFlag()){
                        graphics.drawImage(ImageIO.read(new File("runFile/minesweeping/11.bmp")), 16 * i + 16, 16 * j + 16,null);
                    } else {
                        graphics.drawImage(ImageIO.read(new File("runFile/minesweeping/" + board[i][j].getNumber() + ".bmp")), 16 * i + 16, 16 * j + 16,null);
                    }
                } else {
                    if (board[i][j].isFlag()){
                        graphics.drawImage(ImageIO.read(new File("runFile/minesweeping/10.bmp")), 16 * i + 16, 16 * j + 16,null);
                    } else {
                        graphics.drawImage(ImageIO.read(new File("runFile/minesweeping/12.bmp")), 16 * i + 16, 16 * j + 16,null);
                    }
                }
            }
        }

        for (int i = 0; i < board[0].length; i++) {
            graphics.drawString(String.valueOf(i), 21 + 16 * i, 16);
        }

        for (int j = 0; j < board.length; j++) {
            graphics.drawString(String.valueOf(j), 5, 16 * j + 30);
        }
        return bf;
    }

    /**
     * 根据当前的地图初始化信息，随机放置地雷并配上数字
     */
    public void createBoard() {
        SecureRandom random = new SecureRandom();
        //随机放置sweepNum个雷
        for (int i = 0; i < sweepNum; i++) {
            boolean b = true;
            while (b) {
                int x = random.nextInt(board[0].length);
                int y = random.nextInt(board.length);
                //判断现在有没有雷，没有就放，预防随机到同一个点
                if (board[x][y] == null) {
                    b = false;
                    board[x][y] = new Element(9);
                }
            }
        }

        //双重循环遍历整个地图的节点，给每个节点放置数字
        for (int i = 0; i < board[0].length; i++) {
            for (int j = 0; j < board.length; j++) {
                //只有不是雷的时候才放置
                if (board[i][j] == null || board[i][j].getNumber() != 9) {
                    int n = 0;
                    for (int[] dir : dirs) {
                        //循环判断该节点的周围八个点，一共有几颗雷
                        int x = i + dir[0];
                        int y = j + dir[1];
                        if (x >= 0 && x < board[0].length && y >= 0 && y < board.length && board[x][y] != null && board[x][y].getNumber() == 9) {
                            //不能超距离、不能为空
                            n++;
                        }
                    }
                    board[i][j] = new Element(n);
                }
            }
        }
    }

    /**
     * 选择一个节点翻开，存在以下几种情况
     * 如果是雷：炸
     * 如果是空，采用dfs暴力递归翻开周围的所有节点，如果仍为空，继续递归，不会主动翻开雷
     * @param x 横坐标
     * @param y 纵坐标
     * @return 结果信息封装
     */
    public Info choose(int x, int y) {
        //如果该元素已翻开
        if (board[x][y].isOpen) {
            return new Info(true, "is already open");
        }

        if (board[x][y].getNumber() == 9) {
            //是一颗雷
            return new Info(false, "dead");
        } else if (board[x][y].getNumber() == 0){
            //是空
            dfs(x, y);
        } else {
            //一个普通的数字
            board[x][y].setOpen(true);
            num--;
        }

        if (num > 0) {
            return new Info(true, "go on");
        } else {
            return new Info(false, "victory");
        }
    }

    public void flag(int x, int y) {
        board[x][y].setFlag(!board[x][y].isFlag());
    }

    public void dfs(int x, int y) {
        if (board[x][y].isOpen) {
            return;
        }
        if (!board[x][y].isOpen) {
            board[x][y].setOpen(true);
            num--;
        }
        for (int[] dir : dirs) {
            int newX = x + dir[0];
            int newY = y + dir[1];

            if (newX >= 0 && newX < board[0].length && newY >= 0 && newY < board.length && board[newX][newY] != null) {
                if (board[newX][newY].getNumber() == 0) {
                    dfs(newX, newY);
                } else if (board[newX][newY].getNumber() != 9) {
                    if (!board[newX][newY].isOpen) {
                        board[newX][newY].setOpen(true);
                        num--;
                    }
                }
            }
        }
    }
}
