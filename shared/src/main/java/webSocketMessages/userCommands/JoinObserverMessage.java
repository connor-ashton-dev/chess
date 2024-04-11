package webSocketMessages.userCommands;

public class JoinObserverMessage extends UserGameCommand {
    private final int gameID;

    public JoinObserverMessage(String authToken, int gameID) {
        // initializes og
        super(authToken);
        this.commandType=CommandType.JOIN_OBSERVER;
        this.gameID=gameID;
    }

    public int getGameID() {
        return gameID;
    }
}