package model;

import chess.ChessGame;

import java.util.Objects;

public class GameData {
    int gameId;
    String whiteUsername;
    String blackUsername;
    String gameName;
    ChessGame game;


    public GameData(int id, String wUsername, String bUsername, String name, ChessGame game) {
        this.gameId = id;
        this.whiteUsername = wUsername;
        this.blackUsername = bUsername;
        this.gameName = name;
        this.game = game;
    }

    public int getGameId(){
        return this.gameId;
    }

    public String getWhiteUsername(){
        return this.whiteUsername;
    }
    public String getBlackUsername(){
        return this.blackUsername;
    }

    public String getGameName(){
        return this.gameName;
    }

    public ChessGame getGame(){
        return this.game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameData gameData = (GameData) o;
        return gameId == gameData.gameId && Objects.equals(whiteUsername, gameData.whiteUsername) && Objects.equals(blackUsername, gameData.blackUsername) && Objects.equals(gameName, gameData.gameName) && Objects.equals(game, gameData.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId, whiteUsername, blackUsername, gameName, game);
    }
}
