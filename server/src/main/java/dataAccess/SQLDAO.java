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

    private record DBTuple(int numAffectedRows, int generatedID) {
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

    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public AuthData insertUser(UserData newUser) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData loginUser(UserData myUser) throws DataAccessException {
        return null;
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