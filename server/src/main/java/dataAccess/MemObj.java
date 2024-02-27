package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashMap;

public class MemObj {
    public static HashMap<String, UserData> userMap = new HashMap<>();
    public static HashMap<String, AuthData> tokens = new HashMap<>();

    public static HashMap<Integer, GameData> games = new HashMap<>();

    public static Integer gameId = 0;


    public static void MemClear(){
        MemObj.userMap.clear();
        MemObj.tokens.clear();
        MemObj.games.clear();
    }
}
