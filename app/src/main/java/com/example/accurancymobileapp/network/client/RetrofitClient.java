package com.example.accurancymobileapp.network.client;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getClient(){
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2/accuracyApi/")
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        }
        return retrofit;
    }
}
