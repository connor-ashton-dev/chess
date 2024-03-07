package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public interface DBInterface {
    void clear() throws DataAccessException;

    // user stuff
    AuthData insertUser(UserData newUser) throws DataAccessException;
    AuthData loginUser(UserData myUser) throws DataAccessException;
    void logoutUser(AuthData tok) throws DataAccessException;


    // game stuff
    List<GameData> listGames(AuthData tok) throws DataAccessException;
    void joinGame(AuthData tok, GameData game) throws DataAccessException;
    GameData createGame(AuthData tok, GameData game) throws DataAccessException;
    void updateGame(AuthData tok, GameData game) throws DataAccessException;
    GameData getGame(AuthData tok, int id) throws DataAccessException;

}
