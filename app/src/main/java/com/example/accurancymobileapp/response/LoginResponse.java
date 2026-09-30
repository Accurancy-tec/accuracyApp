package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.User;

public class LoginResponse {
    private boolean success;
    private String message;
    private User user;
    private String token;
    private String refreshToken;

    public boolean isSuccess(){
        return success;
    }

    public String getMessage(){
        return message;
    }

    public User getUser() {
        return user;
    }
    public String getRefreshToken(){return refreshToken;}
    public String getToken() {
        return token;
    }
}
