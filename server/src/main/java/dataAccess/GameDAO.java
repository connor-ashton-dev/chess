package dataAccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.List;

public class GameDAO {
    public List<GameData> listGames(AuthData token) throws DataAccessException {
        if (token == null) throw new DataAccessException("bad request: unauthorized");

        var memTok = MemObj.tokens.get(token.getAuthToken());
        if (memTok == null) throw new DataAccessException("bad request: unauthorized");
        return new ArrayList<>(MemObj.games.values());
    }

    public GameData createGame(AuthData tok, GameData game) throws DataAccessException{
        if (tok == null) throw new DataAccessException("bad request: unauthorized");
        if (game == null) throw new DataAccessException("bad request: missing info");

        var memTok = MemObj.tokens.get(tok.getAuthToken());
        if (memTok == null) throw new DataAccessException("bad request: unauthorized");
        // increment gameID in mem
        MemObj.gameId += 1;
        var myGame = new GameData(MemObj.gameId, game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), new ChessGame());
        MemObj.games.put(MemObj.gameId, myGame);
        return myGame;
    }

    public void joinGame(AuthData tok, GameData game) throws DataAccessException {
        if (tok == null) throw new DataAccessException("bad request: unauthorized");
        if (game == null) throw new DataAccessException("bad request: missing info");

        var memTok = MemObj.tokens.get(tok.getAuthToken());
        if (memTok == null) throw new DataAccessException("bad request: unauthorized");

        var userName = memTok.getUsername();
        var targetGame = MemObj.games.get(game.getGameId());

        if (targetGame == null) throw new DataAccessException("bad request: invalid game");

        if (game.getWhiteUsername() == null && game.getBlackUsername() == null){
            return;
        }

        var wUsername = targetGame.getWhiteUsername();
        var bUsername = targetGame.getBlackUsername();
        var ifUserIsNull = game.getBlackUsername() != null ? bUsername : wUsername;

        if (ifUserIsNull != null) throw new DataAccessException("bad request: username already taken");
        if (game.getWhiteUsername() == null) {
            bUsername = userName;
        } else {
            wUsername = userName;
        }

        var myGame = new GameData(game.getGameId(), wUsername, bUsername, targetGame.getGameName(), targetGame.getGame());

        MemObj.games.put(game.getGameId(), myGame);
    }

    public void updateGame(AuthData tok, GameData replacement, int id) throws DataAccessException {
        if (tok == null) throw new DataAccessException("bad request: unauthorized");
        if (replacement == null) throw new DataAccessException("bad request: missing info");

        var memTok = MemObj.tokens.get(tok.getAuthToken());
        if (memTok == null) throw new DataAccessException("bad request: unauthorized");

        GameData gameToReplace = MemObj.games.get(id);
        if (gameToReplace == null) throw new DataAccessException("bad request: invalid game");

        MemObj.games.put(id, replacement);
    }

    public GameData getGame(AuthData tok, int id) throws DataAccessException{
       if (tok == null) throw new DataAccessException("bad request: unauthorized");

       GameData game = MemObj.games.get(id);
       if (game == null) throw new DataAccessException("bad request: invalid ID");
       return game;
    }






























}
