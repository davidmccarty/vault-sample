package com.garage.data;

public class UserPwd {
    private String username;
    private String password;

    public UserPwd() {
    }

    public UserPwd(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Credential [username=" + username + ", password=" + password + "]";
    }

}
