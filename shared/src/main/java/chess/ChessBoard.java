package chess;

import java.util.HashMap;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    /**
     * HashMap that stores the board state
     * Takes in ChessPosition and ChessPiece
     */
    private final HashMap<ChessPosition, ChessPiece> board;

    public ChessBoard() {
        this.board = new HashMap<>();
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        this.board.put(position, piece);
    }


    private void generateNewBoard() {
        // ----------------------PAWNS----------------------
        // white
        for (int i = 0; i < 8; i++) {
            this.addPiece(new ChessPosition(6, i), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
        }
        // black
        for (int i = 0; i < 8; i++) {
            this.addPiece(new ChessPosition(1, i), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }


        // ----------------------KNIGHTS----------------------
        // white
        this.addPiece(new ChessPosition(0, 1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        this.addPiece(new ChessPosition(0, 6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        // black
        this.addPiece(new ChessPosition(7, 1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        this.addPiece(new ChessPosition(7, 6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));


        // ----------------------ROOKS----------------------
        // white
        this.addPiece(new ChessPosition(0, 0), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        this.addPiece(new ChessPosition(0, 7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        // black
        this.addPiece(new ChessPosition(7, 0), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        this.addPiece(new ChessPosition(7, 7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));


        // ----------------------BISHOPS----------------------
        // white
        this.addPiece(new ChessPosition(0, 2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        this.addPiece(new ChessPosition(0, 5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        // black
        this.addPiece(new ChessPosition(7, 2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        this.addPiece(new ChessPosition(7, 5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));

        // ----------------------QUEENS----------------------
        // white
        this.addPiece(new ChessPosition(0, 4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        // black
        this.addPiece(new ChessPosition(0, 4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));

        // ----------------------KINGS----------------------
        // white
        this.addPiece(new ChessPosition(0, 3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        // black
        this.addPiece(new ChessPosition(0, 3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     *                 position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return this.board.get(position);
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        this.board.clear();
        this.generateNewBoard();
    }

}
