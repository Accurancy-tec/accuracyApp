package com.example.accurancymobileapp.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getClient(){
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2/accurancyApi/")
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        }
        return retrofit;
    }
}
