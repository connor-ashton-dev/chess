package model;

import java.util.Objects;

public class UserData {
   String username;
   String password;
   String email;

   public UserData(String u, String p, String e){
       this.username = u;
       this.password = p;
       this.email = e;
   }


   public String getUsername(){
       return this.username;
   }
    public String getPassword(){
        return this.password;
    }
    public String getEmail(){
        return this.email;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserData userData = (UserData) o;
        return Objects.equals(username, userData.username) && Objects.equals(password, userData.password) && Objects.equals(email, userData.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, email);
    }


    @Override
    public String toString() {
        return "UserData[" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ']';
    }
}
