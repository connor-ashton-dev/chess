import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class ChessboardUI {
    private static final int BOARD_SIZE = 8;
    private static final String EMPTY = "   ";
    private static final String[][] chessPieces = new String[BOARD_SIZE][BOARD_SIZE];


    private static void updateChessPiecesFromGame(ChessGame game) {
        ChessBoard chessBoard = game.getBoard();  // Assuming this method exists and returns the current board state
        ChessPiece[][] board= chessBoard.getSquares();

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                chessPieces[i][j] = board[i][j].parseFromPiece();  // Translate the game's board state to UI's piece representation
            }
        }
    }

    public static void draw(boolean isReversed) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);

        populateArray(isReversed);
        drawHeaders(out, isReversed);
        drawBoard(out, isReversed);
        drawHeaders(out, isReversed);
        out.println();
        out.println();

        setBlackBackground(out);
        setWhiteText(out);
    }

    private static void drawHeaders(PrintStream out, boolean isReversed) {
        setBlackBackground(out);
        String[] headers = {" A ", " B ", " C ", " D ", " E ", " F ", " G ", " H "};
        setLightGreyBackground(out);
        out.print(EMPTY.repeat(1));
        for (int i = !isReversed ? headers.length - 1 : 0; !isReversed ? i >= 0 : i < headers.length; i += !isReversed ? -1 : 1) {
            drawHeader(out, headers[i]);
        }
        setLightGreyBackground(out);
        out.print(EMPTY.repeat(1));
        setBlackBackground(out);
        out.println();
    }

    private static void drawHeader(PrintStream out, String headerText) {
        setLightGreyBackground(out);
        out.print(EMPTY.repeat(1));
        printHeaderText(out, headerText);
        out.print(EMPTY.repeat(1));
        setBlackBackground(out);
    }

    private static void printHeaderText(PrintStream out, String text) {
        setLightGreyBackground(out);
        setWhiteText(out);
        out.print(text);
    }

    private static void populateArray(boolean isReversed) {
        // White pieces
        chessPieces[isReversed ? 7 : 0] = new String[]{"R", "N", "B", "Q", "K", "B", "N", "R"};
        chessPieces[isReversed ? 6 : 1] = new String[]{"P", "P", "P", "P", "P", "P", "P", "P"};

        // Black pieces
        chessPieces[isReversed ? 0 : 7] = new String[]{"r", "n", "b", "q", "k", "b", "n", "r"};
        chessPieces[isReversed ? 1 : 6] = new String[]{"p", "p", "p", "p", "p", "p", "p", "p"};

        // Clear the remaining squares
        for (int row = 2; row < 6; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                chessPieces[row][col] = null;
            }
        }
    }

    private static void drawBoard(PrintStream out, boolean isReversed) {
        int startRow = isReversed ? BOARD_SIZE - 1 : 0;
        int endRow = isReversed ? -1 : BOARD_SIZE;
        int rowIncrement = isReversed ? -1 : 1;

        for (int row = startRow; row != endRow; row += rowIncrement) {
            for (int line = 0; line < 3; line++) {
                drawBoardLine(out, row, line, isReversed);
            }
        }
    }

    private static void drawBoardLine(PrintStream out, int row, int line, boolean isReversed) {
        boolean isEvenRow = row % 2 == 0;
        drawRowNumber(out, row, line, isReversed);
        for (int col = 0; col < BOARD_SIZE / 2; ++col) {
            if (line == 1) {
                drawSquare(out, row, col * 2, isEvenRow, isReversed);
                drawSquare(out, row, col * 2 + 1, !isEvenRow, isReversed);
            } else {
                drawEmptySquare(out, isEvenRow);
                drawEmptySquare(out, !isEvenRow);
            }
        }
        drawRowNumber(out, row, line, isReversed);
        setBlackBackground(out);
        out.println();
    }



    private static void drawRowNumber(PrintStream out, int row, int line, boolean isReversed) {
        if (line == 1) {
            setLightGreyBackground(out);
            setWhiteText(out);

            int rowNumber = row + 1;
            out.print(" " + rowNumber + " ");
        } else {
            setLightGreyBackground(out);
            out.print(EMPTY.repeat(1));
        }
    }


    private static void drawSquare(PrintStream out, int row, int col, boolean isWhite, boolean isReversed) {
        if (isWhite) {
            drawWhiteSquare(out, chessPieces[isReversed ? BOARD_SIZE - 1 - row : row][col], isReversed);
        } else {
            drawBlackSquare(out, chessPieces[isReversed ? BOARD_SIZE - 1 - row : row][col], isReversed);
        }
    }

    private static void drawEmptySquare(PrintStream out, boolean isWhite) {
        if (isWhite) {
            drawWhiteSquare(out, null, false);
        } else {
            drawBlackSquare(out, null, false);
        }
    }

    private static void drawWhiteSquare(PrintStream out, String piece, boolean isReversed) {
        setWhiteBackground(out);
        out.print(EMPTY.repeat(1));
        printPiece(out, piece, true, isReversed);
        out.print(EMPTY.repeat(1));
    }

    private static void drawBlackSquare(PrintStream out, String piece, boolean isReversed) {
        setBlackBackground(out);
        out.print(EMPTY.repeat(1));
        printPiece(out, piece, false, isReversed);
        out.print(EMPTY.repeat(1));
    }

    private static void printPiece(PrintStream out, String piece, boolean isWhite, boolean isReversed) {
        if (piece == null) {
            out.print(EMPTY.repeat(1));
        } else {
            // Adjust text color based on piece type
            if (Character.isUpperCase(piece.charAt(0))) {
                setRedText(out);
            } else {
                setBlueText(out);
            }
            // Adjust piece case based on orientation
            out.print(" " + piece + " ");
            if (isWhite) {
                setWhiteBackground(out);
            } else {
                setBlackBackground(out);
            }
        }
    }

    private static void setWhiteBackground(PrintStream out) {
        out.print(SET_BG_COLOR_WHITE);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void setBlackBackground(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setLightGreyBackground(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
    }

    private static void setWhiteText(PrintStream out) {
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void setRedText(PrintStream out) {
        out.print(SET_TEXT_COLOR_RED);
    }

    private static void setBlueText(PrintStream out) {
        out.print(SET_TEXT_COLOR_BLUE);
    }
}