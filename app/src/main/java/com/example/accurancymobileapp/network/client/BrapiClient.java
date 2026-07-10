package com.example.accurancymobileapp.network.client;

import com.example.accurancymobileapp.network.service.BrapiService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BrapiClient {
    private static Retrofit retrofit;

    public static Retrofit getBrapiClient(){
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://brapi.dev/api/")
                    .client(client)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();

            BrapiService service = retrofit.create(BrapiService.class);
        }
        return retrofit;
    }
}
