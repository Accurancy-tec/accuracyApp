package com.example.accurancymobileapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "id_usuario";
    private static final String KEY_USER_NAME = "nome_usuario";
    private static final String KEY_USER_EMAIL = "email_usuario";
    private static final String KEY_USER_PASSWORD = "senha_usuario";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context){
        sharedPreferences = context.getSharedPreferences(PREF_NAME,context.MODE_PRIVATE);

        editor = sharedPreferences.edit();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        editor.apply();
    }

    public void saveUser(int userId, String userName, String userEmail,String userPass) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_USER_PASSWORD, userPass);

        editor.apply();
    }
    public String getToken() {

        return sharedPreferences.getString(
                KEY_TOKEN,
                null
        );
    }
    public boolean isLoggedIn(){
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId(){
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public String getUserName(){
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail(){
        return sharedPreferences.getString(KEY_USER_EMAIL,"");
    }

    public String getUserPass(){
        return sharedPreferences.getString(KEY_USER_PASSWORD, "");
    }

    public void logout(){
        editor.clear();
        editor.apply();
    }

}
