package model;

import java.util.Objects;
import java.util.UUID;

public class AuthData {
    String authToken;
    String username;

    public AuthData(String u, String t) {
        this.authToken = t;
        this.username = u;
    }


    public AuthData(String u){
        this.authToken = UUID.randomUUID().toString();
        this.username = u;

    }

    public String getAuthToken(){
        return this.authToken;
    }

    public String getUsername(){
        return this.username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthData authData = (AuthData) o;
        return Objects.equals(authToken, authData.authToken) && Objects.equals(username, authData.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authToken, username);
    }
}
