package com.example.accurancymobileapp.network.client;

import com.example.accurancymobileapp.network.service.BrapiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BrapiClient {
    private static Retrofit retrofit;

    public static Retrofit getBrapiClient(){
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://brapi.dev/api/")
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();

            BrapiService service = retrofit.create(BrapiService.class);
        }
        return retrofit;
    }
}
