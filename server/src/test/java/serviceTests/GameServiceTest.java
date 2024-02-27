package serviceTests;

import dataAccess.DataAccessException;
import dataAccess.GameDAO;
import dataAccess.MemObj;
import dataAccess.UserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.GameService;

class GameServiceTest {
    GameDAO gDao;
    UserDAO uDao;
    UserData user;
    GameService gameService;

    // data to use in tests
    GameData newGame = new GameData(240, "", "", "", null);

    @BeforeEach
    // prep the memory of the tests
    void prep() {
        user = new UserData("u", "p", "e");

        gDao = new GameDAO();
        uDao = new UserDAO();
        gameService = new GameService();

        MemObj.MemClear();
    }


    @Test
    void listGamesGood() {
        var tok = assertDoesNotThrow(() -> uDao.insertUser(user));
        var game = assertDoesNotThrow(() -> gDao.createGame(tok, newGame));
        var allGames = assertDoesNotThrow(() -> gameService.listGames(tok));
        assertTrue(allGames.contains(game));
    }


    @Test
    void listGamesBad() {
        var tok = assertDoesNotThrow(() -> uDao.insertUser(user));
        assertDoesNotThrow(() -> gDao.createGame(tok, newGame));

        var badToken = new AuthData(user.getUsername());

        var err = assertThrows(DataAccessException.class, () -> gameService.listGames(badToken));
        assertTrue(err.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    void joinGameGood() {
        var tok = assertDoesNotThrow(() -> uDao.insertUser(user));
        var gameToInsert = new GameData(234234, "wUsername", null, "sickGame", null);

        var game = assertDoesNotThrow(() -> gDao.createGame(tok, gameToInsert));

        assertDoesNotThrow(() -> gameService.joinGame(tok, new GameData(game.getGameId(), null, user.getUsername(), game.getGameName(), null)));
    }

    @Test
    void joinGameBad() {
        var tok = assertDoesNotThrow(() -> uDao.insertUser(user));
        var gameToInsert = new GameData(234234, "wUsername", null, "sickGame", null);

        var game = assertDoesNotThrow(() -> gDao.createGame(tok, gameToInsert));

        var err = assertThrows(DataAccessException.class, () -> gameService.joinGame(new AuthData(user.getUsername()), new GameData(game.getGameId(), null, user.getUsername(), game.getGameName(), null)));
        assertTrue(err.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    void createGameGood() {
        var tok = assertDoesNotThrow(() -> uDao.insertUser(user));
        assertDoesNotThrow(() -> gameService.createGame(tok, newGame));
    }

    @Test
    void createGameBad() {
        var newUser = new AuthData(user.getUsername());
        var err = assertThrows(DataAccessException.class, () -> gameService.createGame(newUser, newGame));
        assertTrue(err.getMessage().toLowerCase().contains("unauthorized"));
    }

}
