package com.example.accurancymobileapp.network.client;

import android.content.Context;
import android.util.Log;

import com.example.accurancymobileapp.BuildConfig;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.utils.AuthInterceptor;
import com.example.accurancymobileapp.utils.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    public static Retrofit getClient(Context context){
        SessionManager sessionManager = new SessionManager(context.getApplicationContext());

        AuthInterceptor interceptor = new AuthInterceptor(sessionManager,context);

        // Logs only in debug builds and never prints credentials (headers and JSON secrets are masked).
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d(
                "HTTP",
                message.replaceAll(
                        "(?i)(\"(?:password|senha|token|access_token|refresh_token|jwt)\"\\s*:\\s*\")[^\"]*(\")",
                        "$1***$2")));
        logging.redactHeader("Authorization");
        logging.redactHeader("Cookie");
        logging.redactHeader("Set-Cookie");

        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_URL)
                    .client(client)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        }
        return retrofit;
    }
    public static ApiService getApiService(Context context) {

        return getClient(context)
                .create(ApiService.class);
    }
}
