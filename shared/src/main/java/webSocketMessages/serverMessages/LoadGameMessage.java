package webSocketMessages.serverMessages;

import chess.ChessGame;
import chess.ChessGame;

public class LoadGameMessage extends ServerMessage {
    private final String game;
    private final ChessGame.TeamColor currentTeam;

    public LoadGameMessage(ChessGame game) {
        super(ServerMessageType.LOAD_GAME);
        this.game=game.serialize();
        currentTeam=game.getTeamTurn();
    }

    public ChessGame getGame() {
        return ChessGame.parseFromString(game, currentTeam);
    }

    @Override
    public String toString() {
        return STR."LoadGameMessage{game='\{game}\{'\''}, currentTeam=\{currentTeam}\{'}'}";
    }
}