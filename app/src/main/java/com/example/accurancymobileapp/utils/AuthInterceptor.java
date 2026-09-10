package com.example.accurancymobileapp.utils;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain)
            throws IOException {

        String token = sessionManager.getToken();

        Request original = chain.request();

        Request request = original.newBuilder()
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .build();

        return chain.proceed(request);
    }
}