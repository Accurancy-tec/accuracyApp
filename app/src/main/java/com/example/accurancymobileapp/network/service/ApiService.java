package com.example.accurancymobileapp.network.service;

import com.example.accurancymobileapp.model.User;
import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.response.AcaoResponse;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.AporteResponse;
import com.example.accurancymobileapp.response.BuscarWalletsResponse;
import com.example.accurancymobileapp.response.CriarWalletResponse;
import com.example.accurancymobileapp.response.LoginResponse;
import com.example.accurancymobileapp.response.QuoteResponse;
import com.example.accurancymobileapp.response.StockHistoryResponse;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/registrarNovoUsuario")
    Call<ResponseBody> registerNewUser(@Body User user);
    @POST("auth/login")
    Call<LoginResponse> loginVerification(@Body User user);
    @POST("auth/refresh")
    Call<LoginResponse> refreshToken(@Body Map<String, String> body);
    @POST("aportes/registrar-aporte")
    Call<ApiResponse> registerAporte(@Body clsAportes aporte);

    //TODO:remover os endpoints usados nessa função
    @GET("aportes/buscar-aportes")
    Call<ApiResponse> getAportes(
            @Query("limite") Integer limite,
            @Query("id_carteira") int idCarteira
    );

    //Criado pra teste
    @GET("aportes/buscar-aportes")
    Call<AporteResponse> buscarAportes(@Query("id_carteira") int idCarteira);
    @GET("aportes/historico-aportes")
    Call<AporteResponse> buscarHistorico();
    @POST("aportes/listar-aportes")
    Call<ApiResponse> buscarLista(@Query("Choise") String escolha);
    @POST("carteiras/criar-carteira")
    Call<CriarWalletResponse> criarNovaWallet(@Body Wallet wallet);
    @GET("carteiras/buscar-carteiras")
    Call<BuscarWalletsResponse> buscarWallets();
    @GET("brapi/get-quote")
    Call<QuoteResponse> getQuote(@Query("symbol") String symbol);

    @GET("brapi/get-symbols")
    Call<QuoteResponse> getService();
    @GET("brapi/listar-acoes-nacionais")
    Call<AcaoResponse> listarAcoesNacionais(
            @Query("page") int page,
            @Query("limit") int limit
    );
    @GET("brapi/historico-acoes")
    Call<StockHistoryResponse> historicoAcoes(
            @Query("symbol") String symbol,
            @Query("range") String range
    );
}
