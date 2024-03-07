package serviceTests;

import dataAccess.DataAccessException;
import dataAccess.MemObj;
import dataAccess.SQLDAO;
import model.AuthData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AuthService;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;


class UserServiceTest {
    SQLDAO dao;

    @BeforeEach
        // prep mem
    void prep() {
        try {
            dao = new SQLDAO();
            dao.clear();
        } catch (Exception err) {
            System.out.println("err:" + err);
        }
    }

    @Test
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
    void loginUserNotFound() {
        var testUser = new UserData("u", null, "e");
        var authService = new AuthService(dao);
        var err = assertThrows(DataAccessException.class, () -> authService.login(testUser));
        // maybe i'll change the error messages to be easier to type one day haha
        assertTrue(err.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    void logoutGood() {
        var testUser = new UserData("u", "p", "e");
        var authService = new AuthService(dao);
        var userService = new UserService(dao);

        assertDoesNotThrow(() -> userService.register(testUser));

        var tok = assertDoesNotThrow(() -> authService.login(testUser));

        assertDoesNotThrow(() -> authService.logout(tok));
    }


    @Test
    void logoutBad() {
        var authService = new AuthService(dao);
        var authToken = new AuthData("username");

        var err = assertThrows(DataAccessException.class, () -> authService.logout(authToken));
        assertTrue(err.getMessage().toLowerCase().contains("unauthorized"));
    }

}
