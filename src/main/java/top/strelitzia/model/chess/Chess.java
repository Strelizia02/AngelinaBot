package top.strelitzia.model.chess;

public abstract class Chess {
    ///抽象棋子类，所有棋子都继承他
    char name;
    boolean isRed;

    int x;
    int y;

    //前进方法
    public abstract Info forward(Chess[][] board, boolean isRed, char order, Move move);

    //后退方法
    public abstract Info back(Chess[][] board, boolean isRed, char order, Move move);

    //平移方法
    public abstract Info lateral(Chess[][] board, boolean isRed, char order, Move move);

    //判断路程是否通畅
    public abstract Info isUnblocked(Chess[][] board, int starX, int starY, int endX, int endY);

    //包装一下，每次都调用这个方法，判断如果不阻挡可以行走，那就直接写入棋谱
    public Info isUnblockedWithMove(Chess[][] board, int starX, int starY, int endX, int endY, Move move) {
        Info info = isUnblocked(board, starX, starY, endX, endY);
        if (info.b) {
            move.startX = starX;
            move.startY = starY;

            move.endX = endX;
            move.endY = endY;

            move.eat = board[endX][endY];
        }
        return info;
    }

    @Override
    public String toString() {
        return "" + name;
    }


    public char getName() {
        return name;
    }

    public void setName(char name) {
        this.name = name;
    }

    public boolean isRed() {
        return isRed;
    }

    public void setRed(boolean red) {
        isRed = red;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
