package top.strelitzia.model.chess;

public class Info {
    String message;
    Chess chess;
    public boolean b;

    public Info(boolean b, String message) {
        this.message = message;
        this.b = b;
    }

    public Info(boolean b, Chess chess, String message) {
        this.message = message;
        this.chess = chess;
        this.b = b;
    }

    @Override
    public String toString() {
        return message;
    }
}
