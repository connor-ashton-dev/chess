package ui;

import java.util.Scanner;
import model.AuthData;
import model.UserData;
import serverFacade.ServerFacade;

public class Main {
    private static ServerFacade serverFacade;
    private static AuthData authData = null; // Holds authentication data once logged in

    public static void main(String[] args) {
        serverFacade = new ServerFacade(); // Assuming localhost and default port
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Chess Game!");
        boolean running = true;

        while (running) {
            if (authData == null) {
                System.out.println("Prelogin Commands: register, login, help, quit");
            } else {
                System.out.println("Postlogin Commands: logout, list, create, join, quit");
            }

            System.out.print("> ");
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
        // Implement registration logic here
    }

    private static void login(String[] params) throws Exception {
        // Implement login logic here
    }

    private static void logout() throws Exception {
        // Implement logout logic here
    }

    private static void listGames() throws Exception {
        // Implement game listing logic here
    }

    private static void createGame(String[] params) throws Exception {
        // Implement game creation logic here
    }

    private static void joinGame(String[] params) throws Exception {
        // Implement game joining logic here
    }

    private static void printHelp() {
        if (authData == null) {
            System.out.println("Available commands: register, login, help, quit");
        } else {
            System.out.println("Available commands: logout, list, create, join, quit");
        }
    }
}
