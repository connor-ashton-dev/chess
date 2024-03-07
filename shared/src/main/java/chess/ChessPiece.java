package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final PieceType pieceType;
    private final ChessGame.TeamColor color;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceType = type;
        this.color = pieceColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceType == that.pieceType && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceType, color);
    }


    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */


    public static ChessPiece parseFromString(char piece) {
        ChessPiece newPiece;

        switch (piece) {
            case 'B' -> newPiece=new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.BISHOP);
            case 'K' -> newPiece=new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.KING);
            case 'Q' -> newPiece=new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.QUEEN);
            case 'P' -> newPiece=new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.PAWN);
            case 'k' -> newPiece=new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.KING);
            case 'q' -> newPiece=new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.QUEEN);
            case 'N' -> newPiece=new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.KNIGHT);
            case 'R' -> newPiece=new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.ROOK);
            case 'p' -> newPiece=new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.PAWN);
            case 'r' -> newPiece=new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.ROOK);
            case 'n' -> newPiece=new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.KNIGHT);
            case 'b' -> newPiece=new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.BISHOP);
            default -> newPiece=null;
        }

        return newPiece;
    }
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        // get the piece I need to analyze
        var piece = board.getPiece(myPosition);

        switch (piece.getPieceType()) {
            case BISHOP -> {
                var bMoves = getBishopMoves(board, myPosition, piece);
                moves.addAll(bMoves);
            }
            case PAWN -> {
                var pMoves = getPawnMoves(board, myPosition, piece);
                moves.addAll(pMoves);
            }
            case KING -> {
                var kMoves = getKingMoves(board, myPosition, piece);
                moves.addAll(kMoves);
            }
            case QUEEN -> {
                var qMoves = getQueenMoves(board, myPosition, piece);
                moves.addAll(qMoves);
            }
            case ROOK -> {
                var rMoves = getRookMoves(board, myPosition, piece);
                moves.addAll(rMoves);
            }
            case KNIGHT -> {
                var kMoves = getKnightMoves(board, myPosition, piece);
                moves.addAll(kMoves);
            }
            case null, default -> {
                return null;
            }
        }

        return moves;
    }

    // ----------------------------------------------------------KNIGHT STUFF -----------------------------------------
    private Collection<ChessMove> getKnightMoves(ChessBoard board, ChessPosition pos, ChessPiece me) {
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {2, 1}, // up 2 right 1
                {2, -1}, // up 2 left 1
                {-2, 1}, // down 2 right 1
                {-2, -1}, // down 2 left 1
                {1, 2}, // up 1 right 2
                {-1, 2}, // down 1 right 2
                {1, -2}, // up 1 left 2
                {-1, -2}, // down 1 left 2
        };
        for (int[] direction : directions) {
            int dr = direction[0];
            int dc = direction[1];
            handleKnightMoves(pos, dr, dc, board, me, moves);
        }
        return moves;
    }

    private void handleKnightMoves(ChessPosition og, int dr, int dc, ChessBoard board, ChessPiece me, HashSet<ChessMove> moves) {
        int nr = og.getRow() + dr;
        int nc = og.getColumn() + dc;

        // is it in bounds?
        if (nr < 1 || nr > 8 || nc < 1 || nc > 8) {
            return;
        }

        var newPos = new ChessPosition(nr, nc);
        var occupiedPiece = board.getPiece(newPos);
        var newMove = new ChessMove(og, newPos, null);
        if (occupiedPiece == null) {
            moves.add(newMove);
        } else {
            if (occupiedPiece.color != me.color) {
                moves.add(newMove);
            }
        }
    }

    // ----------------------------------------------------------ROOK STUFF -----------------------------------------
    private Collection<ChessMove> getRookMoves(ChessBoard board, ChessPosition pos, ChessPiece me) {
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {1, 0}, // up
                {-1, 0}, // down
                {0, -1}, // left
                {0, 1}, // right
        };
        for (int[] direction : directions) {
            int dr = direction[0];
            int dc = direction[1];
            dfs(pos, pos, board, moves, dr, dc, me.color);
        }
        return moves;
    }

    // ----------------------------------------------------------QUEEN STUFF -----------------------------------------
    private Collection<ChessMove> getQueenMoves(ChessBoard board, ChessPosition pos, ChessPiece me) {
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {1, 0}, // up
                {-1, 0}, // down
                {0, -1}, // left
                {0, 1}, // right
                {1, -1}, // up and left
                {1, 1}, // up and right
                {-1, -1}, // down and left
                {-1, 1}, // down and right
        };
        for (int[] direction : directions) {
            int dr = direction[0];
            int dc = direction[1];
            dfs(pos, pos, board, moves, dr, dc, me.color);
        }
        return moves;
    }

    // ----------------------------------------------------------KING STUFF -----------------------------------------
    private Collection<ChessMove> getKingMoves(ChessBoard board, ChessPosition pos, ChessPiece me) {
        HashSet<ChessMove> moves = new HashSet<>();
        int[][] directions = {
                {1, 0}, // up
                {-1, 0}, // down
                {0, -1}, // left
                {0, 1}, // right
                {1, -1}, // up and left
                {1, 1}, // up and right
                {-1, -1}, // down and left
                {-1, 1}, // down and right
        };

        for (int[] direction : directions) {
            int dr = direction[0];
            int dc = direction[1];
            handleKingMove(pos, dr, dc, board, me, moves);
        }
        return moves;
    }

    private void handleKingMove(ChessPosition og, int dr, int dc, ChessBoard board, ChessPiece me, HashSet<ChessMove> moves) {
        int nr = og.getRow() + dr;
        int nc = og.getColumn() + dc;

        // see if new position is OOB
        if (nr < 1 || nr > 8 || nc < 1 || nc > 8) {
            return;
        }

        // otherwise see if anyone is there at the new position
        var newPos = new ChessPosition(nr, nc);
        // if empty just add it
        if (board.getPiece(newPos) == null) {
            var newChessMove = new ChessMove(og, newPos, null);
            moves.add(newChessMove);
        } else {
            // if it's enemy team capture it
            var piece = board.getPiece(newPos);
            if (piece.color != me.color) {
                if (piece.getPieceType() != PieceType.QUEEN){
                    var newChessMove = new ChessMove(og, newPos, null);
                    moves.add(newChessMove);
                }
            }
        }

    }


// ----------------------------------------------------------PAWN STUFF -----------------------------------------

    /**
     * See if we are able to promote
     *
     * @param start og pos
     * @param pos   target pos
     * @param me    piece we are moving
     * @param moves HashMap of moves
     * @return true if we can promote, false if we can't
     */
    private boolean canPromote(ChessPosition start, ChessPosition pos, ChessPiece me, HashSet<ChessMove> moves) {
        if (pos.getRow() == 8 || pos.getRow() == 1) {
            for (PieceType piece : PieceType.values()) {
                if (piece != PieceType.KING && piece != me.pieceType) {
                    var move = new ChessMove(start, pos, piece);
                    moves.add(move);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * If we can promote, handle capturing and promoting
     *
     * @param board       Game board
     * @param pos         our position
     * @param me          our piece
     * @param moves       HashMap of moves
     * @param capture_pos position we are going to capture
     */
    private void handlePromote(ChessBoard board, ChessPosition pos, ChessPiece me, HashSet<ChessMove> moves, ChessPosition capture_pos) {
        if (board.getPiece(capture_pos) != null) {
            if (board.getPiece(capture_pos).color != me.color) {
                boolean didPromote = canPromote(pos, capture_pos, me, moves);
                if (!didPromote) {
                    var newMove = new ChessMove(pos, capture_pos, null);
                    moves.add(newMove);
                }
            }
        }
    }

    /**
     * @param board Game board
     * @param pos   Our position
     * @param me    Our piece
     * @return HashSet of Pawn moves
     */
    private Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition pos, ChessPiece me) {
        HashSet<ChessMove> moves = new HashSet<>();
        int direction = me.color == ChessGame.TeamColor.WHITE ? 1 : -1;

        // -------------------------------- see if OOB
        if (pos.getRow() + direction > 8 || pos.getRow() + direction < 0) {
            return moves;
        }

        // -------------------------------- see if open space
        var newSquare = new ChessPosition(pos.getRow() + direction, pos.getColumn());
        var piece = board.getPiece(newSquare);
        // if there is an open square ahead
        if (piece == null) {
            // can we promote?
            boolean didPromote = canPromote(pos, newSquare, me, moves);
            if (!didPromote) {
                // handle initial positions (jumping 2 squares)
                var nextPos = new ChessPosition(newSquare.getRow() + direction, newSquare.getColumn());
                if (me.color == ChessGame.TeamColor.WHITE && pos.getRow() == 2) {
                    if (board.getPiece(nextPos) == null) {
                        var move2 = new ChessMove(pos, nextPos, null);
                        moves.add(move2);
                    }
                    var move1 = new ChessMove(pos, newSquare, null);
                    moves.add(move1);
                } else if (me.color == ChessGame.TeamColor.BLACK && pos.getRow() == 7) {
                    if (board.getPiece(nextPos) == null) {
                        var move2 = new ChessMove(pos, nextPos, null);
                        moves.add(move2);
                    }
                    var move1 = new ChessMove(pos, newSquare, null);
                    moves.add(move1);
                } else { // non-initial positions
                    var newMove = new ChessMove(pos, newSquare, null);
                    moves.add(newMove);
                }
            }
        }

        // ----------------------------- see if we can capture (up and left or right)
        var pos_capture_left = new ChessPosition(pos.getRow() + direction, pos.getColumn() - 1);
        var pos_capture_right = new ChessPosition(pos.getRow() + direction, pos.getColumn() + 1);

        // see if those captures can make us promote
        handlePromote(board, pos, me, moves, pos_capture_left);
        handlePromote(board, pos, me, moves, pos_capture_right);

        return moves;
    }


// ----------------------------------------------------------BISHOP STUFF -----------------------------------------

    /**
     * Get all Bishop moves
     *
     * @param board Game Board
     * @param pos   Our Position
     * @param me    Our piece
     * @return HashSet of moves
     */
    private Collection<ChessMove> getBishopMoves(ChessBoard board, ChessPosition pos, ChessPiece me) {
        HashSet<ChessMove> moves = new HashSet<>();

        dfs(pos, pos, board, moves, 1, 1, me.getTeamColor());
        dfs(pos, pos, board, moves, -1, 1, me.getTeamColor());
        dfs(pos, pos, board, moves, 1, -1, me.getTeamColor());
        dfs(pos, pos, board, moves, -1, -1, me.getTeamColor());

        return moves;
    }

    /**
     * Perform a DFS in a certain direction, handling adding moves to a HashSet
     *
     * @param ogPos  starting position
     * @param curPos current position
     * @param board  game board
     * @param moves  HashSet of moves
     * @param dr     direction to move in vertically
     * @param dc     direction to move in horizontally
     * @param color  which team we are on (handles captures)
     */
    private void dfs(ChessPosition ogPos, ChessPosition curPos, ChessBoard board, Collection<ChessMove> moves,
                     int dr, int dc, ChessGame.TeamColor color) {
        int row = curPos.getRow();
        int col = curPos.getColumn();
        int nr = row + dr;
        int nc = col + dc;

        var newPos = new ChessPosition(nr, nc);

        // edges
        if (nr < 1 || nr > 8 || nc < 1 || nc > 8) {
            return;
        }

        // collisions
        if (board.getPiece(newPos) != null) {
            // if it's an enemy we can take the square
            var piece = board.getPiece(newPos);
            if (piece.color != color) {
                var newMove = new ChessMove(ogPos, newPos, null);
                moves.add(newMove);
            }
            return;
        }

        // if valid update current position and moves
        var newMove = new ChessMove(ogPos, newPos, null);
        moves.add(newMove);

        // repeat
        dfs(ogPos, newPos, board, moves, dr, dc, color);
    }


}
