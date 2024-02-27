package service;

import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import model.AuthData;
import model.GameData;

import java.util.List;

public class GameService {
    GameDAO dao;

    public GameService(){
        this.dao = new GameDAO();
    }

    public GameService(GameDAO dao){
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

    public void updateGame(AuthData tok, GameData toUpdate, int ogGameId) throws DataAccessException {
        dao.updateGame(tok, toUpdate, ogGameId);
    }

    public GameData getGame(AuthData tok, int id) throws DataAccessException {
        return dao.getGame(tok, id);
    }
}