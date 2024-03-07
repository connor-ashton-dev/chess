package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.List;


public class SQLDao implements DBInterface {
    private static SQLDao DBInstance;


    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public AuthData insertUser(UserData newUser) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData loginUser(UserData myUser) throws DataAccessException {
        return null;
    }

    @Override
    public void logoutUser(AuthData tok) throws DataAccessException {

    }

    @Override
    public List<GameData> listGames(AuthData tok) throws DataAccessException {
        return null;
    }

    @Override
    public void joinGame(AuthData tok, GameData game) throws DataAccessException {

    }

    @Override
    public void updateGame(AuthData tok, GameData game) throws DataAccessException {

    }

    @Override
    public GameData getGame(AuthData tok, int id) throws DataAccessException {
        return null;
    }
}