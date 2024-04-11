package server;

import chess.ChessGame;
import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import service.GameService;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.LoadGameMessage;
import webSocketMessages.serverMessages.NotificationMessage;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class WebsocketGameInfo {
    private static WebsocketGameInfo instance;
    private final GameService gameService;
    private final HashMap<String, Connection> connections=new HashMap<>();
    private final HashMap<Integer, HashSet<Connection>> games=new HashMap<>();
    private final HashSet<Integer> finishedGames=new HashSet<>();

    private WebsocketGameInfo() {
        gameService=new GameService();
    }

    public static WebsocketGameInfo getInstance() {
        if (instance == null) instance=new WebsocketGameInfo();
        return instance;
    }

    public void clear() {
        connections.clear();
        games.clear();
        finishedGames.clear();
    }

    public void addConnection(Session session, AuthData authToken, JoinPlayerMessage message) {
        var connection=new Connection(authToken, session, message.getGameID());
        var gameID=message.getGameID();
        var game=getGame(authToken, gameID);
        if (game == null) {
            sendMessage(new ErrorMessage("Error: no such game"), connection);
            return;
        }
        connections.put(authToken.getAuthToken(), connection);
        if (!games.containsKey(gameID)) games.put(gameID, new HashSet<>());
        games.get(gameID).add(connection);

        if ((message.getPlayerColor() == ChessGame.TeamColor.WHITE && !authToken.getUsername().equals(game.getWhiteUsername())) ||
                (message.getPlayerColor() == ChessGame.TeamColor.BLACK && !authToken.getUsername().equals(game.getBlackUsername()))) {
            sendMessage(new ErrorMessage("Error: player slot already taken"), connection);
            return;
        }

        sendMessage(new LoadGameMessage((ChessGame) game.getGame()), connection);
        broadcast(gameID, new NotificationMessage(STR."\{authToken.getUsername()} has joined as the \{message.getPlayerColor()} player."), authToken.getAuthToken());
    }

    public void addConnection(Session session, AuthData authToken, JoinObserverMessage message) {
        var connection=new Connection(authToken, session, message.getGameID());
        var gameID=message.getGameID();
        var game=getGame(authToken, gameID);
        if (game == null) {
            sendMessage(new ErrorMessage("Error: no such game"), connection);
            return;
        }
        connections.put(authToken.getAuthToken(), connection);
        if (!games.containsKey(gameID)) games.put(gameID, new HashSet<>());
        games.get(gameID).add(connection);

        sendMessage(new LoadGameMessage((ChessGame) game.getGame()), connection);
        broadcast(gameID, new NotificationMessage(STR."\{authToken.getUsername()} has joined as an observer."), authToken.getAuthToken());
    }

    public void makeMove(Session session, AuthData authToken, MakeMoveMessage message) {
        var connection=getConnection(authToken);

        if (connection == null) {
            sendMessage(new ErrorMessage("Error: Cannot make move if you are not in a game!"), new Connection(authToken, session, 0));
            return;
        }

        var game=getGame(authToken, connection.gameID());

        if (!authToken.getUsername().equals(game.getBlackUsername()) && !authToken.getUsername().equals(game.getWhiteUsername())) {
            sendMessage(new ErrorMessage("Error: Cannot make move if you are not a player!"), new Connection(authToken, session, 0));
            return;
        }

        var currentPlayer=game.getGame().getTeamTurn() == ChessGame.TeamColor.WHITE ? game.getWhiteUsername() : game.getBlackUsername();

        if (!authToken.getUsername().equals(currentPlayer)) {
            sendMessage(new ErrorMessage("Error: Cannot make move if it is not your turn!"), new Connection(authToken, session, 0));
            return;
        }

        if (finishedGames.contains(game.getGameId())) {
            sendMessage(new ErrorMessage("Error: Cannot move after the completion of the game!"), new Connection(authToken, session, 0));
            return;
        }

        try {
            game.getGame().makeMove(message.getMove());
            gameService.updateGame(authToken, game);
        } catch (InvalidMoveException | DataAccessException e) {
            sendMessage(new ErrorMessage("Error: " + e.getMessage()), new Connection(authToken, session, 0));
            return;
        }

        var loadGame=new LoadGameMessage((ChessGame) game.getGame());
        var moveMade=new NotificationMessage(authToken.getUsername() + " moved " + message.getMove().toString());

        broadcast(game.getGameId(), loadGame);
        broadcast(game.getGameId(), moveMade, authToken.getAuthToken());

        var g=game.getGame();

        if (g.isInStalemate(g.getTeamTurn())) {
            broadcast(game.getGameId(), new NotificationMessage("The game is a stalemate!"));
            finishedGames.add(game.getGameId());
        } else if (g.isInCheckmate(g.getTeamTurn())) {
            broadcast(game.getGameId(), new NotificationMessage(STR."\{g.getTeamTurn() == ChessGame.TeamColor.BLACK ? game.getBlackUsername() : game.getWhiteUsername()} has been checkmated!"));
            finishedGames.add(game.getGameId());
        } else if (g.isInCheck(g.getTeamTurn()))
            broadcast(game.getGameId(), new NotificationMessage(STR."\{g.getTeamTurn() == ChessGame.TeamColor.BLACK ? game.getBlackUsername() : game.getWhiteUsername()} is in check!"));
    }

    public void leave(Session session, AuthData authToken, LeaveMessage message) {
        var connection=getConnection(authToken);

        if (connection == null) {
            sendMessage(new ErrorMessage("Error: Cannot leave if you are not in a game!"), new Connection(authToken, session, 0));
            return;
        }

        connections.remove(authToken.getAuthToken());
        games.get(message.getGameID()).remove(connection);
        if (session.isOpen()) session.close();

        broadcast(connection.gameID(), new NotificationMessage(STR."\{authToken.getUsername()} has left the game."), authToken.getAuthToken());
    }

    public void resign(Session session, AuthData authToken, ResignMessage message) {
        var connection=getConnection(authToken);

        if (connection == null) {
            sendMessage(new ErrorMessage("Error: Cannot resign if you are not in a game!"), new Connection(authToken, session, 0));
            return;
        }

        var game=getGame(authToken, connection.gameID());

        if (!authToken.getUsername().equals(game.getBlackUsername()) && !authToken.getUsername().equals(game.getWhiteUsername())) {
            sendMessage(new ErrorMessage("Error: Cannot resign if you are not a player!"), new Connection(authToken, session, 0));
            return;
        }

        if (finishedGames.contains(game.getGameId())) {
            sendMessage(new ErrorMessage("Error: Cannot resign after the completion of the game!"), new Connection(authToken, session, 0));
            return;
        }

        broadcast(connection.gameID(), new NotificationMessage(STR."\{authToken.getUsername()} has resigned from the game."));
        connections.remove(authToken.getAuthToken());
        games.get(message.getGameID()).remove(connection);
        if (session.isOpen()) session.close();
        finishedGames.add(connection.gameID());
    }

    public void broadcast(int gameID, ServerMessage message, String excludedAuthToken) {
        var gameConnections=games.get(gameID);

        var deadConnections=new ArrayList<Connection>();

        for (var connection : gameConnections) {
            boolean toSkip=false;
            if (!connection.session().isOpen()) {
                deadConnections.add(connection);
                toSkip=true;
            } else if (excludedAuthToken.equals(connection.authToken().getAuthToken())) toSkip=true;

            if (toSkip) continue;

            sendMessage(message, connection);
        }

        for (var connection : deadConnections) {
            connections.remove(connection.authToken().getAuthToken());
            gameConnections.remove(connection);
        }
    }

    public void sendMessage(ServerMessage message, Connection connection) {
        try {
            System.out.println(STR."Message for \{connection.authToken().getUsername()}: \{message.toString()}");
            connection.session().getRemote().sendString(toJson(message));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void broadcast(int gameID, ServerMessage message) {
        broadcast(gameID, message, "");
    }

    public GameData getGame(AuthData authToken, int gameID) {
        try {
            return gameService.getGame(authToken, gameID);
        } catch (DataAccessException ignored) {
            return null;
        }
    }

    public Connection getConnection(AuthData authToken) {
        return connections.get(authToken.getAuthToken());
    }

    private String toJson(Object o) {
        return new Gson().toJson(o);
    }
}