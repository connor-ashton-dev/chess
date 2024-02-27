package model;


import java.util.Objects;

public record GameHelper(int gameID, String whiteUsername, String blackUsername, String gameName) {
    static public GameHelper fromGame(GameData game) {
        return new GameHelper(game.getGameId(), game.getWhiteUsername(), game.getBlackUsername(), game.getGameName());
    }

    public GameData toGame() {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameHelper that = (GameHelper) o;
        return gameID == that.gameID && Objects.equals(whiteUsername, that.whiteUsername) && Objects.equals(blackUsername, that.blackUsername) && Objects.equals(gameName, that.gameName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameID, whiteUsername, blackUsername, gameName);
    }
}
