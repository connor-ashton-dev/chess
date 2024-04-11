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
            game TEXT not null,
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
          """};

    private interface Adapter<T> {
        T getClass(ResultSet rs) throws SQLException;
    }

    private final Adapter<GameData> gameDataAdapter=rs -> new GameData(
            rs.getInt("id"),
            rs.getString("whitePlayer"),
            rs.getString("blackPlayer"),
            rs.getString("name"),
            ChessGame.parseFromString(
                    rs.getString("game"),
                    ChessGame.TeamColor.values()[rs.getInt("currentTurn")]
            )
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

    private record DBResponse(int numAffectedRows, int generatedID) {
    }

    private <T> ArrayList<T> dbExecute(String action, Adapter<T> adapter, Object... params) throws DataAccessException {
        ArrayList<T> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement preparedAction = getPreparedStatement(conn, action, params);
             ResultSet response = preparedAction.executeQuery()) {

            while (response.next()) {
                results.add(adapter.getClass(response));
            }
        } catch (SQLException e) {
            System.out.println("ERROR IN [dbExecute] " + e.getMessage());
            throw new DataAccessException(e.getMessage());
        }
        return results;
    }

    private DBResponse dbUpdate(String action, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement preparedAction = getPreparedStatement(conn, action, params)) {

            int numAffected = preparedAction.executeUpdate();
            int id = 0;

            try (ResultSet response = preparedAction.getGeneratedKeys()) {
                if (response.next()) {
                    id = response.getInt(1);
                }
            }

            return new DBResponse(numAffected, id);
        } catch (SQLException e) {
            System.out.println("ERROR IN [dbUpdate] " + e.getMessage());
            throw new DataAccessException(e.getMessage());
        }
    }
    private static PreparedStatement getPreparedStatement(Connection conn, String action, Object[] params) throws DataAccessException, SQLException {
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
        try {
            var con = DatabaseManager.getConnection();
            var action = changeSQLActionINfo("select username from %DB_NAME%.authTokens where authToken=?;");
            Adapter<AuthData> tokenAdapter = rs -> new AuthData(rs.getString(1), tok.getAuthToken());
            var res = dbExecute(action, tokenAdapter, tok.getAuthToken());
            return res.isEmpty() ? null : res.getFirst();
        }catch (DataAccessException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var tables = new String[]{"games", "users", "authTokens"};
        for (var table : tables) {
            var action = changeSQLActionINfo("truncate %DB_NAME%." + table);
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
        if (tok == null) throw new DataAccessException("unauthorized");

        var action = changeSQLActionINfo("delete from %DB_NAME%.authTokens where authToken=?;");

        var tup = dbUpdate(action, tok.getAuthToken());
        if (tup.numAffectedRows == 0) throw new DataAccessException("unauthorized");
    }

    @Override
    public List<GameData> listGames(AuthData tok) throws DataAccessException {
        tok = checkTokInDB(tok);

        var action = changeSQLActionINfo("select * from %DB_NAME%.games;");
        return dbExecute(action, gameDataAdapter);
    }

    @Override
    public void joinGame(AuthData tok, GameData game) throws DataAccessException {
        tok = checkTokInDB(tok);
        if (game == null) throw new DataAccessException("bad request");

        var username = tok.getUsername();
        var action = changeSQLActionINfo("select * from %DB_NAME%.games where id=?;");
        var result = dbExecute(action, gameDataAdapter, game.getGameId());

        if (result.isEmpty()) throw new DataAccessException("bad request");
        var target = result.getFirst();

        if (game.getWhiteUsername() == null && game.getBlackUsername() == null) {
            return;
        }

        var whiteUsername = target.getWhiteUsername();
        var blackUsername = target.getBlackUsername();
        System.out.printf("White is :%s and black is :%s\n\n", whiteUsername, blackUsername);
        var nullUserToReplace = game.getBlackUsername() != null ? blackUsername : whiteUsername;

        if (nullUserToReplace != null && !nullUserToReplace.equals("")) throw new DataAccessException("already taken");

        if (game.getWhiteUsername() == null || game.getWhiteUsername().equals("")) blackUsername=username;
        else whiteUsername=username;
        var updateStatement = changeSQLActionINfo("update %DB_NAME%.games set whitePlayer = ?, blackPlayer = ? where id = ?;");

        dbUpdate(updateStatement, whiteUsername, blackUsername, game.getGameId());
    }

    @Override
    public void updateGame(AuthData tok, GameData game) throws DataAccessException {
        tok = checkTokInDB(tok);
        if (game == null) throw new DataAccessException("bad request");

        var id = game.getGameId();

        var action = changeSQLActionINfo("select * from %DB_NAME%.games where id=?;");
        var results = dbExecute(action, gameDataAdapter, id);
        if (results.isEmpty()) throw new DataAccessException("No game");

        var newAction = changeSQLActionINfo("update %DB_NAME%.games set game = ?, currentTurn = ? where id = ?;");
        var myGame = (ChessGame) game.getGame();
        var currentTurn = myGame.getTeamTurn() == ChessGame.TeamColor.WHITE ? 0 : 1;
        dbUpdate(newAction, myGame.serialize(), currentTurn, game.getGameId());
    }

    @Override
    public GameData createGame(AuthData tok, GameData game) throws DataAccessException {
        tok = checkTokInDB(tok);
        if (game == null) throw new DataAccessException("unauthorized");

        var action = changeSQLActionINfo("insert into %DB_NAME%.games (name, game, currentTurn, whitePlayer, blackPlayer) values (?, ?, 0, ?, ?);");
        var target = new ChessGame();
        target.getBoard().resetBoard();

        var tup = dbUpdate(action, game.getGameName(), target.serialize(), game.getWhiteUsername(), game.getBlackUsername());
        return new GameData(tup.generatedID, game.getWhiteUsername(), game.getBlackUsername(), game.getGameName(), new ChessGame());
    }


    @Override
    public GameData getGame(AuthData tok, int id) throws DataAccessException {
        tok = checkTokInDB(tok);
        var action = changeSQLActionINfo("select * from %DB_NAME%.games where id=?;");
        var results = dbExecute(action, gameDataAdapter, id);
        if (results.isEmpty()) throw new DataAccessException("no game found");
        return results.getFirst();
    }


    private AuthData checkTokInDB(AuthData tok) throws DataAccessException {
        if (tok == null) throw new DataAccessException("unauthorized");
        tok = getAuthUser(tok);
        if (tok == null) throw new DataAccessException("unauthorized");
        return tok;
    }

    private AuthData authenticatedUser(AuthData authToken) throws DataAccessException {
        try (var conn=DatabaseManager.getConnection()) {
            var statement=changeSQLActionINfo("select username from %DB_NAME%.authTokens where authToken=?;");
            Adapter<AuthData> tokenAdapter=rs -> new AuthData(authToken.getAuthToken(), rs.getString(1));
            var results=dbExecute(statement, tokenAdapter, authToken.getAuthToken());
            return results.isEmpty() ? null : results.getFirst();
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    public AuthData verifyAuthToken(AuthData authToken) throws DataAccessException {
        if (authToken == null) throw new DataAccessException("unauthorized");
        return authenticatedUser(authToken);
    }
}