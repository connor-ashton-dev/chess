package dataAccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Types.NULL;


public class SQLDAO implements DBInterface {
    private static SQLDAO DBInstance;

    private final String[] createMyStuff = {
            """
          create database if not exists %DB_NAME%;
          """,
            """
          create table if not exists %DB_NAME%.games (
            id int not null auto_increment,
            name varchar(256) not null,
            game char(64) not null,
            currentTurn int not null,
            whitePlayer varchar(256),
            blackPlayer varchar(256),
            primary key (id)
          );
          """,
            """
          create table if not exists %DB_NAME%.users (
            username varchar(256) not null unique,
            password varchar(256) not null,
            email varchar(256) not null,
            primary key (username)
          );
          """,
            """
          create table if not exists %DB_NAME%.authTokens (
            username varchar(256) not null,
            authToken char(36) not null,
            primary key (authToken),
            index(username)
          );
          """
    };

    private interface Adapter<T> {
        T getClass(ResultSet rs) throws SQLException;
    }

    private final Adapter<GameData> gameDataAdapter = rs -> new GameData(
            rs.getInt(1),
            rs.getString(5),
            rs.getString(6),
            rs.getString(2),
            ChessGame.parseFromString(rs.getString(3), ChessGame.TeamColor.values()[rs.getInt(4)])
    );

    public SQLDAO() throws DataAccessException {
        startDB();
    }


    public static SQLDAO getInstance() {
        if (DBInstance != null) return DBInstance;
        try {
            DBInstance = new SQLDAO();
        } catch (Exception err) {
            System.out.println(err.getMessage());
        }
        return DBInstance;
    }


    private String changeSQLActionINfo(String action) {
        return action.replace("%DB_NAME%", DatabaseManager.getDatabaseName());
    }


    private void startDB() throws DataAccessException {
        try {
            var connection = DatabaseManager.getConnection();
            for (var action : createMyStuff) {
                action = changeSQLActionINfo(action);
                var sqlStatement = connection.prepareStatement(action);
                sqlStatement.execute();
            }

        } catch (SQLException err) {
            System.out.println("ERROR OCCURRED IN [startDB]: " + err.getMessage());
        }
    }


    private <T> ArrayList<T> dbExecute(String action, Adapter<T> adapter, Object... params) throws DataAccessException {
        try {
            var preparedAction = getPreparedStatement(action, params);

            var results = new ArrayList<T>();
            var response = preparedAction.executeQuery();
            while (response.next()) {
                results.add(adapter.getClass(response));
            }

            return results;
        } catch (SQLException e) {
            System.out.println("ERROR IN [dbExecute] " + e.getMessage());
            throw new DataAccessException(e.getMessage());
        }
    }

    private record DBResponse(int numAffectedRows, int generatedID) {
    }

    private DBResponse dbUpdate(String action, Object... params) throws DataAccessException {
        try {
            var preparedAction = getPreparedStatement(action, params);
            var numAffected = preparedAction.executeUpdate();
            var id = 0;
            var response = preparedAction.getGeneratedKeys();
            if (response.next()) id = response.getInt(1);
            return new DBResponse(numAffected, id);

        } catch (SQLException e) {
            System.out.println("ERROR IN [dbUpdate] " + e.getMessage());
            throw new DataAccessException(e.getMessage());
        }
    }

    private static PreparedStatement getPreparedStatement(String action, Object[] params) throws DataAccessException, SQLException {
        var conn = DatabaseManager.getConnection();
        var preparedAction = conn.prepareStatement(action, Statement.RETURN_GENERATED_KEYS);

        for (var i = 0; i < params.length; i++) {
            var p = params[i];

            switch (p) {
                case null -> preparedAction.setNull(i + 1, NULL);
                case Integer integer -> preparedAction.setInt(i + 1, integer);
                case String s -> preparedAction.setString(i + 1, s);
                default -> {
                    //idk
                }
            }
        }
        return preparedAction;
    }

    private AuthData getAuthUser(AuthData tok) throws DataAccessException {
        var action = changeSQLActionINfo("select username from %DB_NAME%.authTokens where authToken=?");
        Adapter<AuthData> tokenAdapter = rs -> new AuthData(tok.getAuthToken(), rs.getString(1));
        var res = dbExecute(action, tokenAdapter, tok.getAuthToken());
        return res.isEmpty() ? null : res.getFirst();
    }

    @Override
    public void clear() throws DataAccessException {
        var tables = new String[]{"games", "users", "authTokens"};
        for (var table : tables) {
            var action = changeSQLActionINfo("truncate %DB_NAME%" + table);
            dbUpdate(action);
        }
    }

    @Override
    public AuthData insertUser(UserData newUser) throws DataAccessException {
        if (newUser == null) throw new DataAccessException("bad request");
        if (newUser.getPassword() == null || newUser.getUsername() == null || newUser.getEmail() == null) {
            throw new DataAccessException("bad request");
        }

        var action = changeSQLActionINfo("insert into %DB_NAME%.users values(?,?,?);");
        try {
            dbUpdate(action, newUser.getUsername(), newUser.getPassword(), newUser.getEmail());
        } catch (DataAccessException e) {
            var dup = e.getMessage().contains("Duplicate");
            if (dup) throw new DataAccessException("error: username already taken");
            throw e;
        }

        return loginUser(newUser);
    }

    @Override
    public AuthData loginUser(UserData myUser) throws DataAccessException {
        if (myUser == null) throw new DataAccessException("bad request");

        var userAction = changeSQLActionINfo("select (username) from %DB_NAME%.users where username=? and password=?;");
        Adapter<AuthData> userAdapter = rs -> new AuthData(rs.getString(1));
        var res = dbExecute(userAction, userAdapter, myUser.getUsername(), myUser.getPassword());
        if (res.isEmpty()) throw new DataAccessException("unauthorized");

        var tok = res.getFirst();

        var insertTokAction = changeSQLActionINfo("insert into %DB_NAME%.authTokens values (?, ?);");
        dbUpdate(insertTokAction, tok.getUsername(), tok.getAuthToken());
        return tok;
    }

    @Override
    public void logoutUser(AuthData tok) throws DataAccessException {

    }

    @Override
    public List<GameData> listGames(AuthData tok) throws DataAccessException {
        return null;
    }

    @Override
    public void joinGame(AuthData tok, GameData game) throws DataAccessException {

    }

    @Override
    public void updateGame(AuthData tok, GameData game) throws DataAccessException {

    }

    @Override
    public GameData getGame(AuthData tok, int id) throws DataAccessException {
        return null;
    }
}