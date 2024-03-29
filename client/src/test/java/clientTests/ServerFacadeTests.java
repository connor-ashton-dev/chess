package clientTests;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;
import serverFacade.ServerFacade;
import ui.ClientException;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static ServerFacade serverFacade;
    UserData testUser = new UserData("uniqueUser", "password123", "email@test.com");
    private static Server server;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        serverFacade  = new ServerFacade("localhost", port);
        System.out.println("Started test HTTP server on " + port);
    }
    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setupEnvironment() {
        try {
            serverFacade.clear();
        } catch (ClientException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testClearDatabase() {
        assertDoesNotThrow(() -> serverFacade.clear());
    }

    @Test
    public void testUserRegistrationSuccess() {
        var registrationToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        assertNotNull(registrationToken);
        assertEquals(testUser.getUsername(), registrationToken.getUsername());
        assertNotNull(registrationToken.getAuthToken());
    }
    @Test
    public void testJoinGameNonExistent() {
        AuthData userToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        int nonExistentGameId = 7;
        ClientException exception = assertThrows(ClientException.class,
                () -> serverFacade.joinGame(userToken, nonExistentGameId, "WHITE"));

        assertEquals("Error: bad request", exception.getMessage());
    }


    @Test
    public void testUserRegistrationDuplicate() {
        var firstRegistrationToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        assertNotNull(firstRegistrationToken);
        assertEquals(testUser.getUsername(), firstRegistrationToken.getUsername());
        assertNotNull(firstRegistrationToken.getAuthToken());

        var duplicateRegistrationError = assertThrows(ClientException.class, () -> serverFacade.registerUser(testUser));
        assertEquals("Error: error: username already taken", duplicateRegistrationError.getMessage());
    }

    @Test
    public void testSuccessfulLogin() {
        var registrationToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        assertNotNull(registrationToken);

        var loginToken = assertDoesNotThrow(() -> serverFacade.login(testUser));
        assertNotNull(loginToken);
        assertEquals(testUser.getUsername(), loginToken.getUsername());
        assertNotNull(loginToken.getAuthToken());

        assertNotEquals(registrationToken.getAuthToken(), loginToken.getAuthToken());
    }

    @Test
    public void testLoginFailureUnauthorized() {
        var loginFailure = assertThrows(ClientException.class, () -> serverFacade.login(testUser));
        assertEquals("Error: unauthorized", loginFailure.getMessage());
    }

    @Test
    public void testSuccessfulLogout() {
        var authToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        assertDoesNotThrow(() -> serverFacade.logout(authToken));

        var unauthorizedError = assertThrows(ClientException.class, () -> serverFacade.listGames(authToken));
        assertEquals("Error: unauthorized", unauthorizedError.getMessage());
    }

    @Test
    public void testLogoutWithoutAuthorization() {
        var invalidAuthToken = new AuthData("nonexistentUser");
        var unauthorizedLogoutError = assertThrows(ClientException.class, () -> serverFacade.logout(invalidAuthToken));

        assertEquals("Error: unauthorized", unauthorizedLogoutError.getMessage());
    }

    @Test
    public void testListingGamesAfterCreation() {
        var authToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));

        var createdGame = assertDoesNotThrow(() -> serverFacade.createGame(authToken, "Chess Match"));
        assertNotNull(createdGame);
        var gameId = createdGame.getGameId();
        System.out.println("Game ID after creation: " + gameId);

        var listedGames = assertDoesNotThrow(() -> serverFacade.listGames(authToken));
        assertNotNull(listedGames);
        assertTrue(listedGames.stream().anyMatch(game -> game.getGameId() == gameId));
    }

    @Test
    public void testGameListingUnauthorized() {
        var fakeAuthToken = new AuthData("invalidUser");
        var unauthorizedListGamesError = assertThrows(ClientException.class, () -> serverFacade.listGames(fakeAuthToken));
        assertEquals("Error: unauthorized", unauthorizedListGamesError.getMessage());
    }

    @Test
    public void testCreatingGameSuccessfully() {
        var userToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        var newGame = assertDoesNotThrow(() -> serverFacade.createGame(userToken, "New Chess Game"));
        assertNotNull(newGame);
    }

    @Test
    public void testGameCreationUnauthorized() {
        var unauthorizedToken = new AuthData("invalidUser");
        var creationUnauthorizedError = assertThrows(ClientException.class, () -> serverFacade.createGame(unauthorizedToken, "Unauthorized Game"));
        assertEquals("Error: unauthorized", creationUnauthorizedError.getMessage());
    }

    @Test
    public void testJoiningGameSuccessfully() {
        var userToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        var gameToJoin = assertDoesNotThrow(() -> serverFacade.createGame(userToken, "Joinable Chess Game"));
        assertDoesNotThrow(() -> serverFacade.joinGame(userToken, gameToJoin.getGameId(), "WHITE"));
    }

    @Test
    public void testJoinGameWhenSlotIsOccupied() {
        var authToken=assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        var game=assertDoesNotThrow(() -> serverFacade.createGame(authToken, "game"));
        assertDoesNotThrow(() -> serverFacade.joinGame(authToken, game.getGameId(), "WHITE"));
        var ex=assertThrows(ClientException.class, () -> serverFacade.joinGame(authToken, game.getGameId(), "WHITE"));
        assertEquals("Error: already taken", ex.getMessage());

    }

    @Test
    public void observeGameGood(){
        var userToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        var gameToObserve = assertDoesNotThrow(() -> serverFacade.createGame(userToken, "gonna observe"));
        var doesExist = assertDoesNotThrow(() -> serverFacade.observeGame(userToken, gameToObserve.getGameId()));
        assertTrue(doesExist);
    }
    @Test
    public void observeGameBad() {
        var userToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        var doesExist = assertDoesNotThrow(() -> serverFacade.observeGame(userToken, 500));
        assertFalse(doesExist);

    }
}