package service;

import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import dataAccess.SQLDAO;
import model.AuthData;
import model.GameData;

import java.util.List;

public class GameService {
    SQLDAO dao;


    public GameService(SQLDAO dao){
        this.dao = dao;
    }

    public List<GameData> listGames(AuthData tok) throws DataAccessException {
        return this.dao.listGames(tok);
    }

    public GameData createGame(AuthData tok, GameData game)throws DataAccessException {
        return dao.createGame(tok, game);
    }

    public void joinGame(AuthData tok, GameData targetGame) throws DataAccessException {
        dao.joinGame(tok, targetGame);
    }

}
