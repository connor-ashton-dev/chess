package webSocketMessages.userCommands;

import chess.ChessGame;

public class JoinPlayerMessage extends UserGameCommand {
    private final int gameID;
    private final ChessGame.TeamColor playerColor;

    public JoinPlayerMessage(String authToken, int gameID, ChessGame.TeamColor playerColor) {
        super(authToken);
        this.commandType=CommandType.JOIN_PLAYER;
        this.playerColor=playerColor;
        this.gameID=gameID;
    }

    public int getGameID() {
        return gameID;
    }

    public ChessGame.TeamColor getPlayerColor() {
        return playerColor;
    }
}