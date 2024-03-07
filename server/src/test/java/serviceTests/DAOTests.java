package serviceTests;

import dataAccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DAOTests {
    SQLDAO dao;
     UserData user = new UserData("u", "p", "e");

    @BeforeEach
    void prep() {
        try {
            dao = new SQLDAO();
            dao.clear();
        } catch (Exception e) {
            System.out.println("err" + e);
        }
    }
   DBInterface instantiateDatabase() {
        try {
            var d= new SQLDAO();
            d.clear();
            return d;
        } catch (Exception err) {
            System.out.println("Error: " + err.getMessage());
        }
        return null;
    }

    @Test
    void insertUserSuccess() {
        assertDoesNotThrow(() -> dao.insertUser(user));
    }

    @Test
    void insertUserAlreadyExists() {
        assertDoesNotThrow(() -> dao.insertUser(user));

        var err=assertThrows(DataAccessException.class, () -> dao.insertUser(user));

        assertTrue(err.getMessage().contains("already taken"));
    }

    @Test
    void insertUserBadRequest() {
        UserData badUser = new UserData("u", null, "e");
        var err=assertThrows(DataAccessException.class, () -> dao.insertUser(badUser));

        assertEquals("bad request", err.getMessage());
    }
    @Test
    void loginUserSuccess() {

        assertDoesNotThrow(() -> dao.insertUser(user));

        var token=assertDoesNotThrow(() -> dao.loginUser(user));

        assertNotNull(token);
    }

    @Test
    void loginUserNoSuchUser() {
        var err=assertThrows(DataAccessException.class, () -> dao.loginUser(user));

        assertEquals("unauthorized", err.getMessage());
    }

    @Test
    void loginUserUnauthorized() {

        assertDoesNotThrow(() -> dao.insertUser(user));

        var wrongPasswordUser=new UserData(user.getUsername(), user.getPassword() + "wrong", user.getEmail());

        var err=assertThrows(DataAccessException.class, () -> dao.loginUser(wrongPasswordUser));

        assertEquals("unauthorized", err.getMessage());
    }

    @Test
    void logoutUserSuccess() {
        var authToken=assertDoesNotThrow(() -> dao.insertUser(user));
        assertDoesNotThrow(() -> dao.logoutUser(authToken));
    }

    @Test
    void logoutUserUnauthorized() {
        var authToken=new AuthData(user.getUsername());

        var err=assertThrows(DataAccessException.class, () -> dao.logoutUser(authToken));

        assertEquals("unauthorized", err.getMessage());
    }

    void listGamesSuccess() {
        var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

        var gameToInsert=new GameData(1234);

        var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

        var games=assertDoesNotThrow(() -> dao.listGames(authToken));

        var containsGame=false;

        for (var returnedGame : games) {
            if (returnedGame.equals(game)) {
                containsGame=true;
                break;
            }
        }

        assertTrue(containsGame);
    }

    @Test
    void listGamesUnauthorized() {

        var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

        var gameToInsert=new GameData(1234);

        assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

        var unauthorizedToken=new AuthData(user.getUsername());

        var err=assertThrows(DataAccessException.class, () -> dao.listGames(unauthorizedToken));

        assertEquals("unauthorized", err.getMessage());
    }

    @Test
    void createGameSuccess() {
        var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

        var gameToInsert=new GameData(1234);

        assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));
    }

    @Test
    void createGameSuccessWithTwoGamesSameName() {
        var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

        var gameToInsert1=new GameData(1234, "", "", "gameName", null);
        var gameToInsert2=new GameData(1234, "", "", "gameName", null);

        assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert1));
        assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert2));
    }

    @Test
    void createGameUnauthorized() {

        var gameToInsert=new GameData(1234);

        var err=assertThrows(DataAccessException.class, () -> dao.createGame(new AuthData(user.getUsername()), gameToInsert));

        assertEquals("unauthorized", err.getMessage());
    }

    @Test
    void joinGameSuccess() {

        var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

        var gameToInsert=new GameData(1234, "white", null, "gameName", null);

        var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

        assertDoesNotThrow(() -> dao.joinGame(authToken, new GameData(game.getGameId(), null, user.getUsername(), game.getGameName(), null)));
    }
    @Test
    void joinGameCantBeAlreadySelectedPlayer() {
        var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

        var gameToInsert=new GameData(1234, null, "white", "gameName", null);

        var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

        var err=assertThrows(DataAccessException.class, () -> dao.joinGame(authToken, new GameData(game.getGameId(), null, user.getUsername(), game.getGameName(), null)));

        assertEquals("already taken", err.getMessage());
    }
    @Test
    void joinGameUnauthorized() {
        var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

        var gameToInsert=new GameData(1234, null, "white", "gameName", null);

        var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

        var err=assertThrows(DataAccessException.class, () -> dao.joinGame(new AuthData(user.getUsername()), new GameData(game.getGameId(), null, user.getUsername(), game.getGameName(), null)));

        assertEquals("unauthorized", err.getMessage());
    }

    @Test
    void joinGameCantJoinFullGame() {
        var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

        var gameToInsert=new GameData(1234, "black", "white", "gameName", null);

        var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

        var err=assertThrows(DataAccessException.class, () -> dao.joinGame(authToken, new GameData(game.getGameId(), null, user.getUsername(), game.getGameName(), null)));

        assertEquals("already taken", err.getMessage());
    }

    @Test
    void joinGameCantJoinGameThatDoesNotExist() {
        var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

        var gameToInsert=new GameData(1234, null, "white", "gameName", null);

        var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

        var err=assertThrows(DataAccessException.class, () -> dao.joinGame(authToken, new GameData(game.getGameId() + 1, null, user.getUsername(), game.getGameName(), null)));

        assertEquals("bad request", err.getMessage());
    }

    @Test
    void joinGameObserver() {

        var authToken=assertDoesNotThrow(() -> dao.insertUser(user));

        var gameToInsert=new GameData(1234, null, null, "gameName", null);

        var game=assertDoesNotThrow(() -> dao.createGame(authToken, gameToInsert));

        assertDoesNotThrow(() -> dao.joinGame(authToken, new GameData(game.getGameId(), null, user.getUsername(), game.getGameName(), null)));
    }
}