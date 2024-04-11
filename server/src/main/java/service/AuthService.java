package service;

import dataAccess.DataAccessException;
import dataAccess.SQLDAO;
import dataAccess.UserDAO;
import model.AuthData;
import model.UserData;

public class AuthService {
    SQLDAO dao;

    public AuthService(SQLDAO dao) {
        this.dao = dao;
    }


    public AuthData login(UserData user) throws DataAccessException {
        return dao.loginUser(user);
    }

    public void logout(AuthData tok) throws DataAccessException {
        dao.logoutUser(tok);
    }

    public AuthData verifyAuthToken(AuthData tok) throws DataAccessException {
        return dao.verifyAuthToken(tok);
    }
}
