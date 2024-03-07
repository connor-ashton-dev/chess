package chess;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {


    private final ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {}

    // Copy constructor
    public ChessBoard(ChessBoard other) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPiece originalPiece = other.squares[i][j];
                if (originalPiece != null) {
                    this.squares[i][j] = new ChessPiece(originalPiece.getTeamColor(), originalPiece.getPieceType());
                } else {
                    this.squares[i][j] = null;
                }
            }
        }
    }

    public static ChessBoard parseFromString(String gameString) {
        ChessBoard board = new ChessBoard();
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                var piece = ChessPiece.parseFromString(gameString.charAt(i * 8 + j));
                board.addPiece(new ChessPosition(i, j), piece);
            }
        }
        return board;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChessBoard other = (ChessBoard) obj;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                var p1 = this.squares[i][j];
                var p2 = other.squares[i][j];

                if (p1 == null && p2 == null) {
                    continue;
                }

                // Check if either p1 or p2 is null, but not both
                if (p1 == null || p2 == null) {
                    return false;
                }

                if (p1.getPieceType() != p2.getPieceType() || p1.getTeamColor() != p1.getTeamColor()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(squares);
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "squares=" + Arrays.toString(squares) +
                '}';
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    public void removePiece(ChessPosition position) {
        squares[position.getRow() - 1][position.getColumn() - 1] = null;
    }

    public void movePiece(ChessPosition start, ChessPosition end) {
        ChessPiece piece = getPiece(start);
        this.removePiece(start);
        this.addPiece(end, piece);
    }


    public Collection<ChessPiece> getPieces(ChessGame.TeamColor team) {
        HashSet<ChessPiece> pieces = new HashSet<ChessPiece>();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPosition myPos = new ChessPosition(i, j);
                ChessPiece myPiece = this.getPiece(myPos);
                if (myPiece != null && myPiece.getTeamColor() != team) {
                    pieces.add(myPiece);
                }
            }
        }

        return pieces;
    }

    public ChessPosition getPosition(ChessPiece piece) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = this.getPiece(currentPosition);
                if (currentPiece != null && currentPiece.equals(piece)) {
                    return currentPosition;
                }
            }
        }
        return null; // if the piece is not found on the board
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     *                 position
     */
    public ChessPiece getPiece(ChessPosition position) {
        if (position == null) {
            return null;
        }
        if (position.getRow() < 1 || position.getRow() > 8 || position.getColumn() < 1 || position.getColumn() > 8) {
            return null;
        }
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }


    public Collection<ChessMove> getAllMoves(ChessGame.TeamColor color) {
        Collection<ChessMove> allMoves = new HashSet<>();

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition myPos = new ChessPosition(i, j);
                ChessPiece myPiece = this.getPiece(myPos);
                if (myPiece != null && myPiece.getTeamColor() == color) {
                    Collection<ChessMove> moves = myPiece.pieceMoves(this, myPos);
                    allMoves.addAll(moves);
                }
            }
        }

        return allMoves;
    }



    public ChessPosition findKing(ChessGame.TeamColor color) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition myPos = new ChessPosition(i, j);
                ChessPiece myPiece = this.getPiece(myPos);
                if (myPiece != null && myPiece.getTeamColor() == color && myPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    return myPos;
                }
            }
        }
        return null;
    }


    final static Map<Character, ChessPiece.PieceType> charToTypeMap = Map.of(
            'p', ChessPiece.PieceType.PAWN,
            'n', ChessPiece.PieceType.KNIGHT,
            'r', ChessPiece.PieceType.ROOK,
            'q', ChessPiece.PieceType.QUEEN,
            'k', ChessPiece.PieceType.KING,
            'b', ChessPiece.PieceType.BISHOP);


    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        var boardText = """
                |r|n|b|q|k|b|n|r|
                |p|p|p|p|p|p|p|p|
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                |P|P|P|P|P|P|P|P|
                |R|N|B|Q|K|B|N|R|
                """;
        int row = 8;
        int column = 1;
        for (var c : boardText.toCharArray()) {
            switch (c) {
                case '\n' -> {
                    column = 1;
                    row--;
                }
                case ' ' -> column++;
                case '|' -> {
                }
                default -> {
                    ChessGame.TeamColor color = Character.isLowerCase(c) ? ChessGame.TeamColor.BLACK
                            : ChessGame.TeamColor.WHITE;
                    var type = charToTypeMap.get(Character.toLowerCase(c));
                    var position = new ChessPosition(row, column);
                    var piece = new ChessPiece(color, type);
                    this.addPiece(position, piece);
                    column++;
                }
            }
        }
    }
}
