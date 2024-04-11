package webSocketMessages.userCommands;

import chess.ChessGame;
import chess.ChessMove;
import model.AuthData;

public class UserMessageBase {
    AuthData authToken;

    public UserMessageBase(AuthData authToken) {
        this.authToken=authToken;
    }

    public JoinObserverMessage joinObserver(int gameID) {
        return new JoinObserverMessage(authToken.getAuthToken(), gameID);
    }

    public JoinPlayerMessage joinPlayer(int gameID, ChessGame.TeamColor teamColor) {
        return new JoinPlayerMessage(authToken.getAuthToken(), gameID, teamColor);
    }

    public MakeMoveMessage makeMove(ChessMove move) {
        return new MakeMoveMessage(authToken.getAuthToken(), move);
    }

    public LeaveMessage leave(int gameID) {
        return new LeaveMessage(authToken.getAuthToken(), gameID);
    }

    public ResignMessage resign(int gameID) {
        return new ResignMessage(authToken.getAuthToken(), gameID);
    }
}