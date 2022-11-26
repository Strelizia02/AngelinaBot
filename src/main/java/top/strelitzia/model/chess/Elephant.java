package top.strelitzia.model.chess;

public class Elephant extends Chess {

    public Elephant(boolean isRed, int x, int y) {
        this.isRed = isRed;
        this.x = x;
        this.y = y;
        this.name = '象';
    }

    @Override
    public Info forward(Chess[][] board, boolean isRed, char order, Move move) {
        int step = Board.getIntByCh(order);
        if (isRed) {
            step = 9 - step;
        } else {
            step = step - 1;
        }

        if (step - x != 2 && step - x != -2) {
            return new Info(false, "你移动的太远了");
        }

        if (y == 4) {
            return new Info(false, "象不能过河");
        }

        int yi;
        if (isRed) {
            yi = y + 2;
        } else {
            yi = y - 2;
        }
        Info info = isUnblockedWithMove(board, x, y, step, yi, move);
        if (info.b) {
            board[step][yi] = this;
            board[x][y] = null;
            x = step;
            y = yi;
        }

        return info;
    }

    @Override
    public Info back(Chess[][] board, boolean isRed, char order, Move move) {
        int step = Board.getIntByCh(order);
        if (isRed) {
            step = 9 - step;
        } else {
            step = step - 1;
        }


        if (step - x != 2 && step - x != -2) {
            return new Info(false, "你移动的太远了");
        }

        if (y == 0) {
            return new Info(false, "越界了");
        }

        int yi;
        if (isRed) {
            yi = y - 2;
        } else {
            yi = y + 2;
        }
        Info info = isUnblockedWithMove(board, x, y, step, yi, move);
        if (info.b) {
            board[step][yi] = this;
            board[x][y] = null;
            x = step;
            y = yi;
        }

        return info;
    }

    @Override
    public Info lateral(Chess[][] board, boolean isRed, char order, Move move) {
        return new Info(false, "象没有平移命令");
    }

    @Override
    public Info isUnblocked(Chess[][] board, int starX, int starY, int endX, int endY) {

        int x = (starX + endX) / 2;
        int y = (starY + endY) / 2;

        if (board[x][y] != null) {
            return new Info(false, "有阻挡");
        }

        if (board[endX][endY] != null && board[endX][endY].isRed == this.isRed) {
            return new Info(false, "你不能吃自己的子");
        }

        return new Info(true, "可行");
    }

    @Override
    public String toString() {
        if (isRed) {
            return "相";
        } else {
            return "象";
        }
    }
}
