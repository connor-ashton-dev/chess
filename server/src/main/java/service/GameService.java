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

    public GameService(){
        this.dao = SQLDAO.getInstance();
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

    public GameData getGame(AuthData tok, int gameID) throws DataAccessException {
        return dao.getGame(tok, gameID);
    }
     public void updateGame(AuthData tok, GameData gameToUpdate) throws DataAccessException {
        dao.updateGame(tok, gameToUpdate);
     }

}
