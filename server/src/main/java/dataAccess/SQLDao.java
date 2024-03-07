package dataAccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public class SQLDao implements DBInterface {
    private static SQLDao DBInstance;

    private String[] createMyStuff = {
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

    public SQLDao() throws DataAccessException {
        startDB();
    }


    public static SQLDao getInstance() {
        if (DBInstance != null) return DBInstance;
        try {
            DBInstance = new SQLDao();
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