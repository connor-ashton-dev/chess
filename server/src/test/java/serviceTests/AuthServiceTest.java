package serviceTests;

import dataAccess.DataAccessException;
import dataAccess.MemObj;
import dataAccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AuthService;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    UserDAO dao;
    @BeforeEach
    void prep(){
        dao = new UserDAO();
        MemObj.MemClear();
    }

    @Test
    // test for a good login
    void loginGood() {
        var testUser = new UserData("u", "p", "e");
        var authService = new AuthService(dao);
        var userService = new UserService(dao);

        assertDoesNotThrow(() -> userService.register(testUser));

        var tok = assertDoesNotThrow(() -> authService.login(testUser));

        assertNotNull(tok);
        assertNotNull(tok.getAuthToken());
        assertNotNull(tok.getUsername());
    }

    @Test
    // test for bad login
    void loginUserNotFound(){
        var testUser = new UserData("u", null, "e");
        var authService = new AuthService(dao);
        var err = assertThrows(DataAccessException.class, () -> authService.login(testUser));
        assertTrue(err.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    // test for good logout
    void logoutGood() {
        var testUser = new UserData("u", "p", "e");
        var authService = new AuthService(dao);
        var userService = new UserService(dao);

        assertDoesNotThrow(() -> userService.register(testUser));

        var tok = assertDoesNotThrow(() -> authService.login(testUser));

        assertDoesNotThrow(() -> authService.logout(tok));
    }


    @Test
    // test for bad logout
    void logoutBad(){
        var authService = new AuthService(dao);
        var authToken = new AuthData("username");

        var err = assertThrows(DataAccessException.class, () -> authService.logout(authToken));
        assertTrue(err.getMessage().toLowerCase().contains("unauthorized"));
    }

}
