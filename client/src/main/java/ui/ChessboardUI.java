package ui;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ChessboardUI {
    private static final int BOARD_SIZE = 8;
    private static final String EMPTY_SPACE = "   ";
    private static final String[] PIECES_INITIAL_ROW = {"R", "N", "B", "Q", "K", "B", "N", "R"};
    private static final String[][] chessBoard = new String[BOARD_SIZE][BOARD_SIZE];

    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        initializeChessboard();
        drawChessboard(out);
    }

    private static void initializeChessboard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (i == 0 || i == 7) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    chessBoard[i][j] = (i == 0) ? PIECES_INITIAL_ROW[j] : PIECES_INITIAL_ROW[j].toLowerCase();
                }
            } else if (i == 1 || i == 6) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    chessBoard[i][j] = (i == 1) ? "P" : "p";
                }
            }
        }
    }

    private static void drawChessboard(PrintStream out) {
        drawBorders(out);
        for (int row = 0; row < BOARD_SIZE; row++) {
            drawRow(out, row);
        }
        drawBorders(out);
    }

    private static void drawBorders(PrintStream out) {
        out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        out.print(EMPTY_SPACE);
        String[] headers = {" A ", " B ", " C ", " D ", " E ", " F ", " G ", " H "};
        for (String header : headers) {
            out.print(header);
        }
        out.println(EscapeSequences.SET_BG_COLOR_BLACK); // Reset background color after drawing headers
    }

    private static void drawRow(PrintStream out, int row) {
        for (int line = 0; line < 3; line++) {
            if (line == 1) { // Middle of the square, where the piece is displayed
                for (int col = 0; col < BOARD_SIZE; col++) {
                    String piece = chessBoard[row][col];
                    drawSquare(out, piece, (col + row) % 2 == 0);
                }
            } else { // Top and bottom of the square, empty space
                out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                out.print(EMPTY_SPACE.repeat(BOARD_SIZE));
            }
            out.println();
        }
    }

    private static void drawSquare(PrintStream out, String piece, boolean isWhiteSquare) {
        String bgColor = isWhiteSquare ? EscapeSequences.SET_BG_COLOR_WHITE : EscapeSequences.SET_BG_COLOR_BLACK;
        String textColor = (piece != null && Character.isUpperCase(piece.charAt(0)))
                ? EscapeSequences.SET_TEXT_COLOR_RED
                : EscapeSequences.SET_TEXT_COLOR_BLUE;

        out.print(bgColor + textColor + (piece == null ? EMPTY_SPACE : " " + piece + " "));
    }
}
