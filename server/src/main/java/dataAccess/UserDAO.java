package dataAccess;

import model.AuthData;
import model.UserData;

public class UserDAO {


    public AuthData insertUser(UserData newUser) throws DataAccessException {
        if (newUser == null) throw new DataAccessException("bad request: user is null");

        if (newUser.getPassword()  == null || newUser.getUsername() == null || newUser.getEmail() == null){
            throw new DataAccessException("bad request: missing info");
        }

        var userToInsert= MemObj.userMap.get(newUser.getUsername());
        if (userToInsert != null) throw new DataAccessException("bad request: username already taken");

        MemObj.userMap.put(newUser.getUsername(), newUser);

        return loginUser(newUser);
    }

    public AuthData loginUser(UserData myUser) throws DataAccessException {
        if (myUser == null) throw new DataAccessException("bad request: user is null");

        var authUser = MemObj.userMap.get(myUser.getUsername());
        if (authUser == null) throw new DataAccessException("bad request: Unauthorized");
        if (!authUser.getPassword().equals(myUser.getPassword())) throw new DataAccessException(("unauthorized"));

        var token = new AuthData(myUser.getUsername());
        MemObj.tokens.put(token.getAuthToken(), token);

        return token;
    }

    public void logoutUser(AuthData token) throws DataAccessException {
        if (token == null) throw new DataAccessException("bad request: unauthorized");

        var tokenFromMem = MemObj.tokens.get(token.getAuthToken());
        if (tokenFromMem == null) throw new DataAccessException("bad request: unauthorized");
        MemObj.tokens.remove(tokenFromMem.getAuthToken());
    }

    public AuthData verifyUserToken(AuthData tok){
        if (!MemObj.tokens.containsKey(tok.getAuthToken())) return null;
        return MemObj.tokens.get(tok.getAuthToken());
    }















}
