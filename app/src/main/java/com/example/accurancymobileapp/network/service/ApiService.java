package com.example.accurancymobileapp.network.service;

import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.AporteResponse;
//import com.example.accurancymobileapp.response.BuscarWalletsResponse;
//import com.example.accurancymobileapp.response.CriarWalletResponse;
import com.example.accurancymobileapp.response.BuscarWalletsResponse;
import com.example.accurancymobileapp.response.CriarWalletResponse;
import com.example.accurancymobileapp.response.LoginResponse;
import com.example.accurancymobileapp.model.User;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.response.QuoteResponse;
import com.example.accurancymobileapp.response.TickerResponse;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

//Classe criada para enviar e buscar as informações do banco de dados atráves do php
public interface ApiService {

    @POST("auth/registrarNovoUsuario")
    Call<ResponseBody> registerNewUser(@Body User user);

    @POST("auth/login")
    Call<LoginResponse> loginVerification(@Body User user);

    @POST("aportes/registrar-aporte")
    Call<ApiResponse> registerAporte(@Body clsAportes aporte);

    // limite = quantos aportes trazer (Dashboard usa 4); null = todos
    @GET("aportes/buscar-aportes")
    Call<ApiResponse> getAportes(
            @Query("limite") Integer limite,
            @Query("id_carteira") int id_carteira
    );

    @GET("brapi/get-quotes")
    Call<QuoteResponse> getQuote(
            @Query("symbol") String symbol
    );
    @GET("brapi/get-symbols")
    Call<QuoteResponse> getService();
    @POST("auth/refresh")
    Call<LoginResponse> refreshToken(@Body Map<String, String> body);

    //Criado pra teste
    @GET("aportes/buscar-aportes")
    Call<AporteResponse> buscarAportes(
            @Query("id_carteira") int idCarteira
    );

    @POST("carteiras/criar-carteira")
    Call<CriarWalletResponse> criarNovaWallet(
            @Body Wallet wallet
    );

    @GET("carteiras/buscar-carteiras")
    Call<BuscarWalletsResponse> buscarWallets();

    @GET("aportes/historico-aportes")
    Call<AporteResponse> buscarHistorico();
}
