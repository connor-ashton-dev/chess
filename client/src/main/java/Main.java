import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import model.AuthData;
import model.GameData;
import model.UserData;
import serverFacade.ServerFacade;
import ui.ChessboardUI;
import ui.ClientException;

public class Main {
    private static ServerFacade serverFacade;
    private static AuthData authData = null; // Holds authentication data once logged in

    public static void main(String[] args) {
        serverFacade = new ServerFacade("localhost", 8080);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Chess Game!");
        boolean running = true;

        while (running) {
            if (authData == null) {
                System.out.print("[LOGGED_OUT] ");
            } else {
                System.out.print("[LOGGED_IN] ");
            }

            System.out.print(">>> ");
            String input = scanner.nextLine();
            String[] inputs = input.split("\\s+");
            String command = inputs[0].toLowerCase();
            String[] params = new String[inputs.length - 1];
            System.arraycopy(inputs, 1, params, 0, inputs.length - 1);

            try {
                switch (command) {
                    case "register":
                        register(params);
                        break;
                    case "login":
                        login(params);
                        break;
                    case "logout":
                        logout();
                        break;
                    case "list":
                        listGames();
                        break;
                    case "create":
                        createGame(params);
                        break;
                    case "join":
                        joinGame(params);
                        break;
                    case "observe":
                        observeGame(params);
                        break;
                    case "quit":
                        System.out.println("Exiting application.");
                        running = false;
                        break;
                    case "help":
                    default:
                        printHelp();
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void register(String[] params) throws Exception {
        if (params.length != 3) {
            System.out.println("Usage: register <username> <password> <email>");
            return;
        }

        String username = params[0];
        String password = params[1];
        String email = params[2];

        try {
            UserData userData = new UserData(username, password, email);
            authData = serverFacade.registerUser(userData);
            System.out.println("Registration successful. You are now logged in.");
        } catch (ClientException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private static void login(String[] params) throws Exception {
        if (params.length != 3) {
            System.out.println("Usage: login <username> <password>");
            return;
        }
        String username = params[0];
        String password = params[1];
        String email= params[2];
        try {
            UserData userData = new UserData(username, password, email);
            authData = serverFacade.login(userData);
            System.out.println("Login successful.");
        } catch (ClientException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }


    private static void logout() throws Exception {
        if (authData == null) {
            System.out.println("You are not logged in.");
            return;
        }
        try {
            serverFacade.logout(authData);
            authData = null; // Clear authentication data
            System.out.println("Logout successful.");
        } catch (ClientException e) {
            System.out.println("Logout failed: " + e.getMessage());
        }
    }

    private static void listGames() throws Exception {
        if (authData == null) {
            System.out.println("You must be logged in to list games.");
            return;
        }
        try {
            List<GameData> games = serverFacade.listGames(authData);
            if (games.isEmpty()) {
                System.out.println("No games available.");
            } else {
                System.out.println("Available games:");
                for (int i = 0; i < games.size(); i++) {
                    String gameName = STR."\{i + 1}: \{games.get(i).getGameName()}";
                    String whitePlayer = games.get(i).getWhiteUsername();
                    String blackPlayer = games.get(i).getBlackUsername();

                    // Check for white player presence
                    String whiteDisplay = whitePlayer == null || whitePlayer.isEmpty() ? "WHITE: no players" : "WHITE: " + whitePlayer;
                    // Check for black player presence
                    String blackDisplay = blackPlayer == null || blackPlayer.isEmpty() ? "BLACK: no players" : "BLACK: " + blackPlayer;

                    // Ensure "no players" is only shown if both are absent
                    if (whiteDisplay.equals("no players") && blackDisplay.equals("no players")) {
                        System.out.println(gameName + ": no players");
                    } else {
                        System.out.println(gameName + ": " + whiteDisplay + " " + blackDisplay);
                    }                }
            }
        } catch (ClientException e) {
            System.out.println("Failed to list games: " + e.getMessage());
        }
    }

    private static void createGame(String[] params) throws Exception {
        if (authData == null) {
            System.out.println("You must be logged in to create a game.");
            return;
        }
        if (params.length < 1) {
            System.out.println("Usage: create <gameName>");
            return;
        }
        try {
            String gameName = String.join(" ", Arrays.copyOfRange(params, 0, params.length));
            serverFacade.createGame(authData, gameName);
            System.out.println("Game created successfully.");
        } catch (ClientException e) {
            System.out.println("Failed to create game: " + e.getMessage());
        }
    }

    private static void joinGame(String[] params) throws Exception {
        if (authData == null) {
            System.out.println("You must be logged in to join a game.");
            return;
        }
        if (params.length != 2) {
            System.out.println("Usage: join <gameID> <playerColor>");
            return;
        }
        try {
            int gameID = Integer.parseInt(params[0]); // Assuming gameID is the first parameter
            String playerColor = params[1]; // Assuming playerColor is the second parameter
            serverFacade.joinGame(authData, gameID, playerColor);
            ChessboardUI.draw(false);
            ChessboardUI.draw(true);
        } catch (NumberFormatException e) {
            System.out.println("Invalid game ID.");
        } catch (ClientException e) {
            System.out.println("Failed to join game: " + e.getMessage());
        }
    }

    private static void observeGame(String[] params) throws Exception {
        if (authData == null) {
            System.out.println("You must be logged in to observe a game.");
            return;
        }
        if (params.length != 1) {
            System.out.println("Usage: observe <gameID>");
            return;
        }
        try {
            int gameID = Integer.parseInt(params[0]); // Assuming gameID is the first parameter
            if (serverFacade.observeGame(authData, gameID)){
                ChessboardUI.draw(false);
                ChessboardUI.draw(true);
                System.out.println("Observed game successfully.");
            }else{
                System.out.println("Game not found");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid game ID.");
        } catch (ClientException e) {
            System.out.println("Failed to observe game: " + e.getMessage());
        }
    }

    private static void printHelp() {
        if (authData == null) {
            System.out.println("Available commands:\nregister <username> <password> <email>\nlogin <username> <password>\nhelp\nquit");
        } else {
            System.out.println("Available commands:\nlogout\nlist\ncreate <name>\njoin\nquit");
        }
    }
}
