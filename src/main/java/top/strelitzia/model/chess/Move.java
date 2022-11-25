package top.strelitzia.model.chess;

public class Move {
    int startX;

    int startY;

    int endX;

    int endY;

    Chess move;

    Chess eat;

    //谁走的
    Long player;

    @Override
    public String toString() {
        return "Move{" +
                "startX=" + startX +
                ", startY=" + startY +
                ", endX=" + endX +
                ", endY=" + endY +
                ", move=" + move +
                ", eat=" + eat +
                ", player=" + player +
                '}';
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public int getEndY() {
        return endY;
    }

    public void setEndY(int endY) {
        this.endY = endY;
    }

    public Chess getMove() {
        return move;
    }

    public void setMove(Chess move) {
        this.move = move;
    }

    public Chess getEat() {
        return eat;
    }

    public void setEat(Chess eat) {
        this.eat = eat;
    }

    public Long getPlayer() {
        return player;
    }

    public void setPlayer(Long player) {
        this.player = player;
    }
}