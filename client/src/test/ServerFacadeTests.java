import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serverFacade.ServerFacade;
import ui.ClientException;

import static org.junit.jupiter.api.Assertions.*;

class ServerFacadeTests {

    ServerFacade server=new ServerFacade();
    UserData user=new UserData("user", "pass", "email");

    @BeforeEach
    void setUp() {
        try {
            server.clear();
        } catch (ClientException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void clear() {
        assertDoesNotThrow(() -> server.clear());
    }

    @Test
    void registerUserSuccess() {
        var authToken=assertDoesNotThrow(() -> server.registerUser(user));
        assertNotNull(authToken);
        assertEquals(user.getUsername(), authToken.getUsername());
        assertNotNull(authToken.getAuthToken());
    }

    @Test
    void registerUserAlreadyExists() {
        var authToken=assertDoesNotThrow(() -> server.registerUser(user));
        assertNotNull(authToken);
        assertEquals(user.getUsername(), authToken.getUsername());
        assertNotNull(authToken.getAuthToken());
        var ex=assertThrows(ClientException.class, () -> server.registerUser(user));
        assertEquals("Error: error: username already taken", ex.getMessage());
    }

    @Test
    void loginSuccess() {
        var authToken=assertDoesNotThrow(() -> server.registerUser(user));
        assertNotNull(authToken);
        assertEquals(user.getUsername(), authToken.getUsername());
        assertNotNull(authToken.getAuthToken());

        var loginAuthToken=assertDoesNotThrow(() -> server.login(user));
        assertNotNull(loginAuthToken);
        assertEquals(user.getUsername(), loginAuthToken.getUsername());
        assertNotNull(loginAuthToken.getAuthToken());

        assertNotEquals(authToken.getAuthToken(), loginAuthToken.getAuthToken());
    }

    @Test
    void loginUnauthorized() {
        var ex=assertThrows(ClientException.class, () -> server.login(user));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void logoutSuccess() {
        var authToken=assertDoesNotThrow(() -> server.registerUser(user));
        assertDoesNotThrow(() -> server.logout(authToken));
        var ex=assertThrows(ClientException.class, () -> server.listGames(authToken));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void logoutUnauthorized() {
        var authToken=new AuthData("user");
        var ex=assertThrows(ClientException.class, () -> server.logout(authToken));

        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void listGamesSuccess() {
        var authToken=assertDoesNotThrow(() -> server.registerUser(user));
        assertNotNull(authToken);
        assertEquals(user.getUsername(), authToken.getUsername());
        assertNotNull(authToken.getAuthToken());

        var game=assertDoesNotThrow(() -> server.createGame(authToken, "game"));
        System.out.println(game.getGameId());
        assertNotNull(game);
        var games=assertDoesNotThrow(() -> server.listGames(authToken));
        assertNotNull(games);
        assertEquals(games.getFirst().getGameId(), game.getGameId());
    }

    @Test
    void listGamesUnauthorized() {
        var authToken=new AuthData("user");
        var ex=assertThrows(ClientException.class, () -> server.listGames(authToken));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void createGameSuccess() {
        var authToken=assertDoesNotThrow(() -> server.registerUser(user));
        var game=assertDoesNotThrow(() -> server.createGame(authToken, "game"));
        assertNotNull(game);
    }

    @Test
    void createGameUnauthorized() {
        var authToken=new AuthData("user");
        var ex=assertThrows(ClientException.class, () -> server.createGame(authToken, "game"));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void joinGameSuccess() throws ClientException {
        var authToken=assertDoesNotThrow(() -> server.registerUser(user));
        var game=assertDoesNotThrow(() -> server.createGame(authToken, "game"));
        System.out.println(game.getGameId());
        assertDoesNotThrow(() -> server.joinGame(authToken, game.getGameId(), "WHITE"));
    }

    @Test
    void joinGameSlotTaken() {
        var authToken=assertDoesNotThrow(() -> server.registerUser(user));
        var game=assertDoesNotThrow(() -> server.createGame(authToken, "game"));
        assertDoesNotThrow(() -> server.joinGame(authToken, game.getGameId(), "WHITE"));
        var ex=assertThrows(ClientException.class, () -> server.joinGame(authToken, game.getGameId(), "WHITE"));
        assertEquals("Error: already taken", ex.getMessage());
    }
}