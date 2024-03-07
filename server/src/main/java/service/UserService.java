package service;

import dataAccess.DataAccessException;
import dataAccess.SQLDAO;
import dataAccess.UserDAO;
import model.AuthData;
import model.UserData;

public class UserService {
    SQLDAO dao;

    public UserService(SQLDAO dao){
       this.dao = dao;
    }
    public AuthData register(UserData user) throws DataAccessException {
        return dao.insertUser(user);
    }
    public AuthData login(UserData user) throws DataAccessException{
        return dao.loginUser(user);
    }
}
