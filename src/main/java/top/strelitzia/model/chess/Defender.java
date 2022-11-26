package top.strelitzia.model.chess;

public class Defender extends Chess {

    public Defender(boolean isRed, int x, int y) {
        this.isRed = isRed;
        this.x = x;
        this.y = y;
        this.name = '士';
    }

    @Override
    public Info forward(Chess[][] board, boolean isRed, char order, Move move) {
        int step = Board.getIntByCh(order);
        //未来的地址
        int yi;
        int xi;
        if (order != '四' && order != '五' && order != '六') {
            return new Info(false, "越界了");
        }

        if (isRed) {
            yi = y + 1;
            xi = 9 - step;
        } else {
            yi = y - 1;
            xi = step - 1;
        }

        if (yi < 7 && yi > 2) {
            return new Info(false, "越界了");
        }

        Info info = isUnblockedWithMove(board, x, y, xi, yi, move);
        if (info.b) {
            board[xi][yi] = this;
            board[x][y] = null;
            y = yi;
            x = xi;
        }
        return info;
    }

    @Override
    public Info back(Chess[][] board, boolean isRed, char order, Move move) {
        int step = Board.getIntByCh(order);
        //未来的地址
        int yi;
        int xi;
        if (order != '四' && order != '五' && order != '六') {
            return new Info(false, "越界了");
        }

        if (isRed) {
            yi = y - 1;
            xi = 9 - step;
        } else {
            yi = y + 1;
            xi = step - 1;
        }

        if (yi < 0 || yi > 9) {
            return new Info(false, "越界了");
        }

        Info info = isUnblockedWithMove(board, x, y, xi, yi, move);
        if (info.b) {
            board[xi][yi] = this;
            board[x][y] = null;
            y = yi;
            x = xi;
        }
        return info;
    }

    @Override
    public Info lateral(Chess[][] board, boolean isRed, char order, Move move) {
        return new Info(false, "士不能平移");
    }

    @Override
    public Info isUnblocked(Chess[][] board, int starX, int starY, int endX, int endY) {
        if (starX == endX) {
            return new Info(false, "您搁着原地踏步？");
        }

        if (board[endX][endY] != null && board[endX][endY].isRed == this.isRed) {
            return new Info(false, "你不能吃自己的子");
        }

        return new Info(true, "可行");
    }

    @Override
    public String toString() {
        if (isRed) {
            return "士";
        } else {
            return "仕";
        }
    }
}