package server;

import com.google.gson.Gson;
import dataAccess.*;
import model.AuthData;
import model.GameData;
import model.GameHelper;
import model.UserData;
import service.AuthService;
import service.GameService;
import service.UserService;
import spark.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Server {
    GameService gameService;
    UserService userService;
    AuthService authService;

    public Server(){
        GameDAO gDao = new GameDAO();
        UserDAO uDAO = new UserDAO();
//        AuthDAO aDAO = new AuthDAO();

        gameService = new GameService(gDao);
        userService = new UserService(uDAO);
        authService = new AuthService(uDAO);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");


        // create a game
        Spark.post("/game", this::createGame);
        // list games
        Spark.get("/game", this::listGames);
        // join a game
        Spark.put("/game", this::joinGame);

        // register a new user
        Spark.post("/user", this::registerUser);

        // login a user
        Spark.post("/session", this::login);
        // logout a user
        Spark.delete("/session", this::logout);

        Spark.delete("/db", this::deleteData);


        Spark.awaitInitialization();
        return Spark.port();
    }


    String getTokFromHeaders(Request req) {
        return req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");
    }


    private Object logout(Request req, Response res){
        String tok = getTokFromHeaders(req);
        AuthData authTok = new AuthData("", tok);
        try {
            authService.logout(authTok);
            res.status(200);
            return "{}";
        } catch (DataAccessException err) {
            return dbErrors(err, req, res);
        }
    }

    private Object login(Request req, Response res){
        var myUser = new Gson().fromJson(req.body(), UserData.class);
        AuthData tok;
        try {
            tok = authService.login(myUser);
            res.status(200);

            Map<String, Object> response = new HashMap<>();
            response.put("username", tok.getUsername());
            response.put("authToken", tok.getAuthToken());

            System.out.println(tok.getAuthToken() + ":" + tok.getUsername());

            String jsonResponse = new Gson().toJson(response);
            res.body(jsonResponse);

            return jsonResponse;
        } catch (DataAccessException err) {
            return dbErrors(err, req, res);
        }
    }


    private Object joinGame(Request req, Response res) {
        String tok = getTokFromHeaders(req);
        AuthData authToken = new AuthData("", tok);
        System.out.println(req.body());
        try {
            var body = new Gson().fromJson(req.body(), HashMap.class);
            int gameId = (int) Math.round((Double) body.get("gameID"));
            var color = (String) body.get("playerColor");
            var black = "BLACK".equals(color) ? color : null;
            var white = "WHITE".equals(color) ? color : null;
            var game = new GameData(gameId, white, black, null, null);
            gameService.joinGame(authToken, game);
            res.status(200);
            return "{}";
        } catch (DataAccessException err) {
            return dbErrors(err, req, res);
        }
    }


    private Object listGames(Request req, Response res) {
        String authToken = getTokFromHeaders(req);
        AuthData tok = new AuthData("", authToken);
        try {
            var games = gameService.listGames(tok);
            ArrayList<GameHelper> gameHelperData = new ArrayList<>();
            for (var game : games) {
                gameHelperData.add(GameHelper.fromGame(game));
            }
            res.status(200);
            return getObjFromJSon(Collections.singletonMap("games", gameHelperData));
        } catch (DataAccessException err) {
            return dbErrors(err, req, res);
        }
    }


    private Object registerUser(Request req, Response res) {
        var user = new Gson().fromJson(req.body(), UserData.class);
        AuthData tok;
        try {
            tok = userService.register(user);
            res.status(200);
            res.body(getObjFromJSon(tok));
            return getObjFromJSon(tok);
        } catch (DataAccessException err) {
            return dbErrors(err, req, res);
        }
    }

    private record ErrorResponse(String message) {
        ErrorResponse(String message) {
            this.message = "Error: " + message;
        }
    }

    private String getErrorFromJson(String message) {
        return new Gson().toJson(new ErrorResponse(message));
    }

    private String getObjFromJSon(Object obj) {
        return new Gson().toJson(obj);
    }

    private Object dbErrors(DataAccessException err, Request req, Response res) {
        int status;
        var body = getErrorFromJson(err.getMessage());
        System.out.println(body);

        if (err.getMessage().toLowerCase().contains("unauthorized")) {
            status = 401;
        } else if (err.getMessage().toLowerCase().contains("already taken")) {
            status = 403;
        } else {
            status = 400;
        }

        res.type("application/json");
        res.body(body);
        res.status(status);
        return body;
    }

    private Object deleteData(Request req, Response res) {
        MemObj.userMap.clear();
        MemObj.games.clear();
        MemObj.tokens.clear();
        res.status(200);
        res.body("{}");
        return "{}";
    }

    private Object createGame(Request req, Response res) {
        String authToken = getTokFromHeaders(req);
        AuthData tok = new AuthData("", authToken);
        try {
            var game = gameService.createGame(tok, new Gson().fromJson(req.body(), GameData.class));
            res.status(200);
            return getObjFromJSon(Collections.singletonMap("gameID", game.getGameId()));
        } catch (DataAccessException err) {
            return dbErrors(err, req, res);
        }
    }


    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}