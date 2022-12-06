package minesweeping;

import chess.Chess;

public class Info {
    String message;
    public boolean b;

    public Info(boolean b, String message) {
        this.message = message;
        this.b = b;
    }

    public Info(boolean b, Chess chess, String message) {
        this.message = message;
        this.b = b;
    }

    @Override
    public String toString() {
        return message;
    }
}
