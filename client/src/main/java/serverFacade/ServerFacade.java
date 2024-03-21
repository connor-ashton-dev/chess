package serverFacade;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.GameHelper;
import model.UserData;
import ui.ClientException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServerFacade {
    private final String serverUrl;
    private final int serverPort;

    public ServerFacade(String serverUrl, int serverPort) {
        this.serverUrl = serverUrl;
        this.serverPort = serverPort;
    }

    public ServerFacade() {
        this("localhost", 8080);
    }

    public void clear() throws ClientException {
        makeRequest("DELETE", "db", null, null, null);
    }

    public AuthData registerUser(UserData user) throws ClientException {
        return makeRequest("POST", "user", user, AuthData.class, null);
    }

    public AuthData login(UserData user) throws ClientException {
        return makeRequest("POST", "session", user, AuthData.class, null);
    }

    public void logout(AuthData authToken) throws ClientException {
        makeRequest("DELETE", "session", null, null, authToken);
    }

    public List<GameData> listGames(AuthData authToken) throws ClientException {
        GamesList games = makeRequest("GET", "game", null, GamesList.class, authToken);
        System.out.println(games);
        return games.games().stream().map(GameHelper::toGame).toList();
    }

    public GameData createGame(AuthData authToken, String gameName) throws ClientException {
        return makeRequest("POST", "game", new GameData(gameName), GameData.class, authToken);
    }

    public void joinGame(AuthData authToken, int gameID, String playerColor) throws ClientException {
        makeRequest("PUT", "game", new JoinGameRequest(gameID, playerColor), null, authToken);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, AuthData authToken) throws ClientException {
        try {
            HttpURLConnection connection = setupConnection(path, method, authToken);
            if (request != null) {
                sendData(request, connection);
            }
            verifyResponseCode(connection);
            return (responseClass == null) ? null : readData(connection, responseClass);
        } catch (IOException ex) {
            throw new ClientException(HttpURLConnection.HTTP_BAD_REQUEST, ex.getMessage());
        }
    }

    private HttpURLConnection setupConnection(String path, String method, AuthData authToken) throws IOException, ClientException {
        URL url = buildUrl(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setReadTimeout(5000);
        if (authToken != null) {
            connection.setRequestProperty("Authorization", authToken.getAuthToken());
        }
        return connection;
    }

    private URL buildUrl(String path) throws ClientException {
        try {
            return new URI("http", null, serverUrl, serverPort, "/" + path, null, null).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new ClientException(400, e.getMessage());
        }
    }

    private void sendData(Object data, HttpURLConnection connection) throws IOException {
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        try (var os = connection.getOutputStream()) {
            byte[] input = new Gson().toJson(data).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private void verifyResponseCode(HttpURLConnection connection) throws IOException, ClientException {
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            ErrorResponse errorResponse = readData(connection, ErrorResponse.class);
            throw new ClientException(connection.getResponseCode(), errorResponse.message());
        }
    }

    public <T> T readData(HttpURLConnection connection, Class<T> classOfT) throws ClientException {
        try {
            InputStream responseBody;
            try {
                responseBody=connection.getInputStream();
            } catch (IOException ignored) {
                responseBody=connection.getErrorStream();
            }
            var reader=new BufferedReader(new InputStreamReader(responseBody));
            var response=new StringBuilder();
            String line;

            while ((line=reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            responseBody.close();

            return new Gson().fromJson(response.toString(), classOfT);
        } catch (IOException ex) {
            throw new ClientException(HttpURLConnection.HTTP_BAD_REQUEST, ex.getMessage());
        }
    }

    private InputStream getResponseBody(HttpURLConnection connection) throws IOException {
        InputStream responseBody;
        try {
            responseBody = connection.getInputStream();
        } catch (IOException ignored) {
            responseBody = connection.getErrorStream();
        }
        return responseBody;
    }

    private record ErrorResponse(String message) {
    }

    private record GamesList(ArrayList<GameHelper> games) {
    }

    private record JoinGameRequest(int gameID, String playerColor) {
    }
}