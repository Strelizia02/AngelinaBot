package top.strelitzia.model.minesweeping;


public class Info {
    String message;
    public boolean b;

    public Info(boolean b, String message) {
        this.message = message;
        this.b = b;
    }

    @Override
    public String toString() {
        return message;
    }
}
