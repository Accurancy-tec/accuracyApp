package com.example.accurancymobileapp.api;

import com.example.accurancymobileapp.model.LoginResponse;
import com.example.accurancymobileapp.model.User;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("registerNewUser.php")
    Call<ResponseBody> registerNewUser(@Body User user);

    @POST("login.php")
    Call<LoginResponse> loginVerification(@Body User user);
}
