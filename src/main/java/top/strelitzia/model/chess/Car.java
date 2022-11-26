package top.strelitzia.model.chess;

public class Car extends Chess {
    public Car(boolean isRed, int x, int y) {
        this.isRed = isRed;
        this.x = x;
        this.y = y;
        this.name = '车';
    }

    @Override
    public Info forward(Chess[][] board, boolean isRed, char order, Move move) {
        int step = Board.getIntByCh(order);
        //未来的地址
        int yi;
        if (isRed) {
            yi = y + step;
            if (yi > 9) {
                return new Info(false, "越界了");
            }
        } else {
            yi = y - step;
            if (yi < 0) {
                return new Info(false, "越界了");
            }
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
        int step = Board.getIntByCh(order);
        int yi;
        if (!isRed) {
            if (y + step <= 9) {
                yi = y + step;
            } else {
                return new Info(false, "越界了");
            }
        } else {
            if (y - step >= 0) {
                yi = y - step;
            } else {
                return new Info(false, "越界了");
            }
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
    public Info lateral(Chess[][] board, boolean isRed, char order, Move move) {
        int step = Board.getIntByCh(order);
        int xi;
        if (isRed) {
            xi = 9 - step;
        } else {
            xi = step - 1;
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
            return new Info(false, "宁搁着原地踏步？");
        }

        if (starX == endX) {
            int max = endY;
            int min = starY;
            if (starY > endY) {
                max = starY;
                min = endY;
            }
            for (int j = min + 1; j < max; j++) {
                if (board[starX][j] != null) {
                    return new Info(false, "有阻挡");
                }
            }
        }

        if (starY == endY) {
            int max = endX;
            int min = starX;
            if (starX > endX) {
                max = starX;
                min = endX;
            }
            for (int i = min + 1; i < max; i++) {
                if (board[i][starY] != null) {
                    return new Info(false, "有阻挡");
                }
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
            return "车";
        } else {
            return "車";
        }
    }
}