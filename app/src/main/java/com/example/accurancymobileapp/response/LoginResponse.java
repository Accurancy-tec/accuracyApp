package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.User;

public class LoginResponse {
    private boolean success;
    private String message;
    private User user;
    private String token;
    private String refreshToken;
    private boolean email_nao_verificado;
    private String email_usuario;

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

    /** True when the credentials are right but the e-mail still has to be confirmed with the code. */
    public boolean isEmailNaoVerificado() {
        return email_nao_verificado;
    }

    public String getEmailUsuario() {
        return email_usuario;
    }

    public String getToken() {
        return token;
    }
}
