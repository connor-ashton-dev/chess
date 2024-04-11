package webSocketMessages.userCommands;

import chess.ChessMove;

public class MakeMoveMessage extends UserGameCommand {
    private final ChessMove move;

    public MakeMoveMessage(String authToken, ChessMove move) {
        super(authToken);
        this.commandType=CommandType.MAKE_MOVE;
        this.move=move;
    }

    public ChessMove getMove() {
        return move;
    }
}