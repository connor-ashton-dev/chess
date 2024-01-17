package chess;

import java.util.ArrayList;
import java.util.Collection;

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
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        // get the piece I need to analyze
        var piece = board.getPiece(myPosition);

        switch (piece.getPieceType()){
            case BISHOP -> {
               var bMoves = getBishopMoves(board, myPosition, piece);
               moves.addAll(bMoves);
            }
            case null, default -> {
               return null;
            }
        }

        return moves;
    }


    private Collection<ChessMove> getBishopMoves(ChessBoard board, ChessPosition pos, ChessPiece me){
        ArrayList<ChessMove> moves = new ArrayList<>();

        dfs(pos, pos, board, moves, 1, 1, me.getTeamColor());
        dfs(pos, pos, board, moves, -1, 1, me.getTeamColor());
        dfs(pos, pos, board, moves, 1, -1, me.getTeamColor());
        dfs(pos, pos, board, moves, -1, -1, me.getTeamColor());

        return moves;
    }

    private void dfs(ChessPosition ogPos, ChessPosition curPos, ChessBoard board, Collection<ChessMove> moves, int dr, int dc, ChessGame.TeamColor color){
        var row = curPos.getRow();
        var col = curPos.getColumn();
        var nr = row + dr;
        var nc = col + dc;

        var newPos = new ChessPosition(nr, nc);

        // edges
        if (nr < 1 || nr > 8 || nc < 1 || nc > 8){
            return;
        }

        // collisions
        if (board.getPiece(newPos) != null){
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
