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

class ServerFacadeTests {

    ServerFacade serverFacade = new ServerFacade();
    UserData testUser = new UserData("uniqueUser", "password123", "email@test.com");
    private static Server server;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
    }
    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setupEnvironment() {
        try {
            serverFacade.clear();
        } catch (ClientException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void testClearDatabase() {
        assertDoesNotThrow(() -> serverFacade.clear());
        assertDoesNotThrow(() -> serverFacade.clear());
    }

    @Test
    void testUserRegistrationSuccess() {
        var registrationToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        assertNotNull(registrationToken);
        assertEquals(testUser.getUsername(), registrationToken.getUsername());
        assertNotNull(registrationToken.getAuthToken());
    }

    @Test
    void testUserRegistrationDuplicate() {
        var firstRegistrationToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        assertNotNull(firstRegistrationToken);
        assertEquals(testUser.getUsername(), firstRegistrationToken.getUsername());
        assertNotNull(firstRegistrationToken.getAuthToken());

        var duplicateRegistrationError = assertThrows(ClientException.class, () -> serverFacade.registerUser(testUser));
        assertEquals("Error: error: username already taken", duplicateRegistrationError.getMessage());
    }

    @Test
    void testSuccessfulLogin() {
        var registrationToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        assertNotNull(registrationToken);

        var loginToken = assertDoesNotThrow(() -> serverFacade.login(testUser));
        assertNotNull(loginToken);
        assertEquals(testUser.getUsername(), loginToken.getUsername());
        assertNotNull(loginToken.getAuthToken());

        assertNotEquals(registrationToken.getAuthToken(), loginToken.getAuthToken());
    }

    @Test
    void testLoginFailureUnauthorized() {
        var loginFailure = assertThrows(ClientException.class, () -> serverFacade.login(testUser));
        assertEquals("Error: unauthorized", loginFailure.getMessage());
    }

    @Test
    void testSuccessfulLogout() {
        var authToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        assertDoesNotThrow(() -> serverFacade.logout(authToken));

        var unauthorizedError = assertThrows(ClientException.class, () -> serverFacade.listGames(authToken));
        assertEquals("Error: unauthorized", unauthorizedError.getMessage());
    }

    @Test
    void testLogoutWithoutAuthorization() {
        var invalidAuthToken = new AuthData("nonexistentUser");
        var unauthorizedLogoutError = assertThrows(ClientException.class, () -> serverFacade.logout(invalidAuthToken));

        assertEquals("Error: unauthorized", unauthorizedLogoutError.getMessage());
    }

    @Test
    void testListingGamesAfterCreation() {
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
    void testGameListingUnauthorized() {
        var fakeAuthToken = new AuthData("invalidUser");
        var unauthorizedListGamesError = assertThrows(ClientException.class, () -> serverFacade.listGames(fakeAuthToken));
        assertEquals("Error: unauthorized", unauthorizedListGamesError.getMessage());
    }

    @Test
    void testCreatingGameSuccessfully() {
        var userToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        var newGame = assertDoesNotThrow(() -> serverFacade.createGame(userToken, "New Chess Game"));
        assertNotNull(newGame);
    }

    @Test
    void testGameCreationUnauthorized() {
        var unauthorizedToken = new AuthData("invalidUser");
        var creationUnauthorizedError = assertThrows(ClientException.class, () -> serverFacade.createGame(unauthorizedToken, "Unauthorized Game"));
        assertEquals("Error: unauthorized", creationUnauthorizedError.getMessage());
    }

    @Test
    void testJoiningGameSuccessfully() {
        var userToken = assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        var gameToJoin = assertDoesNotThrow(() -> serverFacade.createGame(userToken, "Joinable Chess Game"));
        assertDoesNotThrow(() -> serverFacade.joinGame(userToken, gameToJoin.getGameId(), "WHITE"));
    }

    @Test
    void testJoinGameWhenSlotIsOccupied() {
        var authToken=assertDoesNotThrow(() -> serverFacade.registerUser(testUser));
        var game=assertDoesNotThrow(() -> serverFacade.createGame(authToken, "game"));
        assertDoesNotThrow(() -> serverFacade.joinGame(authToken, game.getGameId(), "WHITE"));
        var ex=assertThrows(ClientException.class, () -> serverFacade.joinGame(authToken, game.getGameId(), "WHITE"));
        assertEquals("Error: already taken", ex.getMessage());

    }
}