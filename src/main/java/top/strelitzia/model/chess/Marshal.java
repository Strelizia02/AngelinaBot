package top.strelitzia.model.chess;

public class Marshal extends Chess {

    public Marshal(boolean isRed, int x, int y) {
        this.isRed = isRed;
        this.x = x;
        this.y = y;
        this.name = '帅';
    }

    @Override
    public Info forward(Chess[][] board, boolean isRed, char order, Move move) {
        //未来的地址
        int yi;
        if (order != '一') {
            int step = Board.getIntByCh(order);
            Chess eat;
            if (isRed) {
                eat = board[x][y + step];
            } else {
                eat = board[x][y - step];
            }

            if (eat == null || eat.name != '帅' || eat.isRed == isRed) {
                return new Info(false, "只能走一步");
            } else {
                if (isRed) {
                    yi = y + step;
                } else {
                    yi = y - step;
                }
            }
        } else {
            if (isRed) {
                yi = y + 1;
            } else {
                yi = y - 1;
            }
        }

        if ((yi > 2 && yi < 7) || yi > 9 || yi < 0) {
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
        if (order != '一') {
            return new Info(false, "只能走一步");
        }
        //未来的地址
        int yi;
        if (isRed) {
            yi = y - 1;
        } else {
            yi = y + 1;
        }

        if ((yi > 2 && yi < 7) || yi > 9 || yi < 0) {
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
    public Info lateral(Chess[][] board, boolean isRed, char order, Move move) {
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

        if (!(xi >= 3 && xi <= 5)) {
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

        if (board[endX][endY] != null && board[endX][endY].name == '帅' && board[endX][endY].isRed != isRed) {
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
            return new Info(true, "将帅会晤");
        }
        return new Info(true, "可行");
    }

    @Override
    public String toString() {
        if (isRed) {
            return "帅";
        } else {
            return "将";
        }
    }
}