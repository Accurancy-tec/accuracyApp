package com.example.accurancymobileapp.api;

import com.example.accurancymobileapp.model.User;

public class LoginResponse {
    private boolean success;
    private String message;
    private User user;

    public boolean isSuccess(){
        return success;
    }

    public String getMessage(){
        return message;
    }

    public User getUser() {
        return user;
    }
}
