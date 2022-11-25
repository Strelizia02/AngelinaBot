package top.strelitzia.model.chess;

public class Soldier extends Chess {

    public Soldier(boolean isRed, int x, int y) {
        this.isRed = isRed;
        this.x = x;
        this.y = y;
        this.name = '兵';
    }

    @Override
    public Info forward(Chess[][] board, boolean isRed, char order, Move move) {
        //未来的地址
        int yi;
        if (order != '一') {
            return new Info(false, "只能走一步");
        }

        if (isRed) {
            yi = y + 1;
        } else {
            yi = y - 1;
        }

        if (yi > 9 || yi < 0) {
            return new Info(false, "越界了");
        }

        Info info = isUnblockedWithMove(board, x, y, x, yi, move);
        if (info.b) {
            board[x][yi] = this;
            board[x][y] = null;
            y = yi;
        }
        return info;
    }

    @Override
    public Info back(Chess[][] board, boolean isRed, char order, Move move) {
        return new Info(false, "不能后退");
    }

    @Override
    public Info lateral(Chess[][] board, boolean isRed, char order, Move move) {
        if (isRed && y < 5 || !isRed && y > 4) {
            return new Info(false, "没过河不能平移");
        }
        int step = Board.getIntByCh(order);
        //未来的地址
        int xi;
        if (isRed) {
            xi = 9 - step;
        } else {
            xi = step - 1;
        }

        if (x - xi != 1 && x - xi != -1) {
            return new Info(false, "只能走一步");
        }

        if (!(xi >= 0 && xi <= 9)) {
            return new Info(false, "越界了");
        }

        Info info = isUnblockedWithMove(board, x, y, xi, y, move);
        if (info.b) {
            board[xi][y] = this;
            board[x][y] = null;
            x = xi;
        }
        return info;
    }

    @Override
    public Info isUnblocked(Chess[][] board, int starX, int starY, int endX, int endY) {
        if (starX == endX && starY == endY) {
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
            return "兵";
        } else {
            return "卒";
        }
    }
}