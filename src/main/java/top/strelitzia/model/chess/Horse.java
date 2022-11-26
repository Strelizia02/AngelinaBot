package top.strelitzia.model.chess;

public class Horse extends Chess {

    public Horse(boolean isRed, int x, int y) {
        this.isRed = isRed;
        this.x = x;
        this.y = y;
        this.name = '马';
    }

    @Override
    public Info forward(Chess[][] board, boolean isRed, char order, Move move) {
        int step = Board.getIntByCh(order);
        //未来的地址
        int yi;
        int xi;
        int up = 1;
        if (isRed) {
            xi = 9 - step;
        } else {
            xi = step - 1;
            up = -1;
        }

        if (x - xi == 1) {
            yi = y + 2 * up;
        } else if (x - xi == -1) {
            yi = y + 2 * up;
        } else if (x - xi == 2) {
            yi = y + up;
        } else if (x - xi == -2) {
            yi = y + up;
        } else {
            return new Info(false, "马走日");
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
        int up = -1;
        if (isRed) {
            xi = 9 - step;
        } else {
            xi = step - 1;
            up = 1;
        }

        if (x - xi == 1) {
            yi = y + 2 * up;
        } else if (x - xi == -1) {
            yi = y + 2 * up;
        } else if (x - xi == 2) {
            yi = y + up;
        } else if (x - xi == -2) {
            yi = y + up;
        } else {
            return new Info(false, "马走日");
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
        return new Info(false, "马没有平移");
    }

    @Override
    public Info isUnblocked(Chess[][] board, int starX, int starY, int endX, int endY) {
        if (starX == endX && starY == endY) {
            return new Info(false, "您搁着原地踏步？");
        }

        if (starX - endX == 2) {
            //横着左跳
            if (board[starX - 1][starY] != null) {
                return new Info(false, "有阻挡");
            }
        } else if (starX - endX == -2) {
            //横着右跳
            if (board[starX + 1][starY] != null) {
                return new Info(false, "有阻挡");
            }
        } else if (starY - endY == -2) {
            //竖着上跳
            if (board[starX][starY + 1] != null) {
                return new Info(false, "有阻挡");
            }
        } else if (starY - endY == 2) {
            //竖着下跳
            if (board[starX][starY - 1] != null) {
                return new Info(false, "有阻挡");
            }
        }


        if (board[endX][endY] != null && board[endX][endY].isRed == this.isRed) {
            return new Info(false, "你不能吃自己的子");
        }
        return new Info(true, "可行");
    }

    @Override
    public String toString() {
        if (isRed) {
            return "马";
        } else {
            return "馬";
        }
    }
}