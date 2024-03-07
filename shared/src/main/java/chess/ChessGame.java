package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private ChessBoard copyBoard;
    private TeamColor turn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        turn = TeamColor.WHITE;
    }


    public static ChessGame parseFromString(String gameString, TeamColor curTurn) {
        var game = new ChessGame();
        game.setTeamTurn(curTurn);
        game.setBoard(ChessBoard.parseFromString(gameString));
        return game;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }

    private void switchTeamTurn() {
        this.turn = this.turn == TeamColor.BLACK ? TeamColor.WHITE : TeamColor.BLACK;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     *
     */

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        // this is because I'm making the turn to the current piece, and switching it back
        TeamColor oldColor = this.turn;

        ChessPiece myPiece = this.board.getPiece(startPosition);

        if (myPiece == null) {
            return new HashSet<>(); // Return an empty set if no piece, or it's not the piece's turn
        }

        this.turn = myPiece.getTeamColor();

        HashSet<ChessMove> finalMoves = new HashSet<>();
        Collection<ChessMove> moves = myPiece.pieceMoves(this.board, startPosition);
        ChessBoard clonedBoard = new ChessBoard(this.board); // Clone the board to simulate moves

        for (ChessMove move : moves) {
            ChessPiece capturedPiece = clonedBoard.getPiece(move.end); // Save the captured piece
            clonedBoard.movePiece(move.start, move.end); // Make the move

            if (!isOtherBoardInCheck(myPiece.getTeamColor(), clonedBoard)) {
                finalMoves.add(move);
            }

            // Undo the move
            clonedBoard.movePiece(move.end, move.start);
            clonedBoard.addPiece(move.end, capturedPiece); // Restore the captured piece, if any
        }

        this.turn = oldColor;
        return finalMoves;
    }


    private boolean validateMove(ChessMove move, ChessPiece piece) {
        ChessPosition start = move.start;
        ChessPosition end = move.end;

        if (OOB(end) || OOB(start)) {
            return false;
        }

        if (piece.getTeamColor() != this.turn) {
            return false;
        }

        return true;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.start;
        ChessPosition end = move.end;

        ChessPiece piece = board.getPiece(start);
        if (piece == null) {
            throw new InvalidMoveException("Invalid move");
        }

        if (!validateMove(move, piece)) {
            throw new InvalidMoveException("Invalid move");
        }

        Collection<ChessMove> validMoves = validMoves(start);

        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        this.board.removePiece(start);
        this.board.addPiece(end, piece);

        // Check for Pawn Promotion
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            // Check if the pawn has reached the end of the board for promotion
            if ((piece.getTeamColor() == TeamColor.WHITE && end.getRow() == 8) ||
                    (piece.getTeamColor() == TeamColor.BLACK && end.getRow() == 1)) {
                // Handle promotion
                if (move.getPromotionPiece() == null) {
                    throw new InvalidMoveException("Pawn reached the end of the board but no promotion piece was specified.");
                }
                // Replace the pawn with the promotion piece
                this.board.removePiece(end);
                ChessPiece promotionPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
                this.board.addPiece(end, promotionPiece);
            }
        }

        switchTeamTurn();
    }

    private boolean OOB(ChessPosition pos) {
        return (pos.getRow() < 1 || pos.getRow() > 8 || pos.getColumn() < 1 || pos.getColumn() > 8);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        TeamColor otherTeam = teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
        Collection<ChessMove> otherTeamMoves = board.getAllMoves(otherTeam);
        ChessPosition kingPos = board.findKing(teamColor);

        for (ChessMove move : otherTeamMoves) {
            if (move.getEndPosition().equals(kingPos)) {
                return true;
            }
        }

        return false;
    }

    public boolean isOtherBoardInCheck(TeamColor teamColor, ChessBoard otherBoard) {
        TeamColor otherTeam = teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;

        Collection<ChessMove> otherTeamMoves = otherBoard.getAllMoves(otherTeam);
        ChessPosition kingPos = otherBoard.findKing(teamColor);

        for (ChessMove move : otherTeamMoves) {
            if (move.getEndPosition().equals(kingPos)) {
                return true;
            }
        }

        return false;
    }

    public Collection<ChessMove> allValidMoves(TeamColor teamColor, ChessBoard myBoard) {
        Collection<ChessMove> allMoves = new HashSet<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition myPos = new ChessPosition(i, j);
                ChessPiece chessPiece = myBoard.getPiece(myPos);
                if (chessPiece != null && chessPiece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(myPos);
                    allMoves.addAll(moves);
                }
            }
        }

        return allMoves;
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false; // Not in checkmate if not in check
        }
        return allValidMoves(teamColor, board).isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)){
           return false;
        }

        return allValidMoves(teamColor, board).isEmpty();
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
