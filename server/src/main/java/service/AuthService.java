package service;

import dataAccess.DataAccessException;
import dataAccess.UserDAO;
import model.AuthData;
import model.UserData;

public class AuthService {
    UserDAO dao;

    public AuthService(UserDAO dao) {
        this.dao = dao;
    }


    public AuthData login(UserData user) throws DataAccessException {
        return dao.loginUser(user);
    }

    public void logout(AuthData tok) throws DataAccessException {
        dao.logoutUser(tok);
    }
}
