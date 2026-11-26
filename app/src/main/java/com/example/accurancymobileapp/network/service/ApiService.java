package com.example.accurancymobileapp.network.service;

import com.example.accurancymobileapp.model.AdicionarSaldoRequest;
import com.example.accurancymobileapp.model.NovaCarteiraRequest;
import com.example.accurancymobileapp.model.User;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.response.AcaoResponse;
import com.example.accurancymobileapp.response.AdicionarSaldoResponse;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.AporteResponse;
import com.example.accurancymobileapp.response.AssetPositionResponse;
import com.example.accurancymobileapp.response.BuscarWalletsResponse;
import com.example.accurancymobileapp.response.CriarWalletResponse;
import com.example.accurancymobileapp.response.DepositoSimulacaoResponse;
import com.example.accurancymobileapp.response.FavoritosResponse;
import com.example.accurancymobileapp.response.LoginResponse;
import com.example.accurancymobileapp.response.NoticiasResponse;
import com.example.accurancymobileapp.response.NotificacoesResponse;
import com.example.accurancymobileapp.response.PerfilResponse;
import com.example.accurancymobileapp.response.PerguntasResponse;
import com.example.accurancymobileapp.response.QuoteResponse;
import com.example.accurancymobileapp.response.RespostaSimples;
import com.example.accurancymobileapp.response.ResumoPatrimonioResponse;
import com.example.accurancymobileapp.response.SessoesResponse;
import com.example.accurancymobileapp.response.StockHistoryResponse;
import com.example.accurancymobileapp.response.TitulosRendaFixaResponse;

import java.math.BigDecimal;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/registrarNovoUsuario")
    Call<ResponseBody> registerNewUser(@Body User user);

    @POST("auth/login")
    Call<LoginResponse> loginVerification(@Body User user);

    @POST("auth/refresh")
    Call<LoginResponse> refreshToken(@Body Map<String, String> body);

    // ---- E-mail verification and Google sign-in (no token needed) ----

    @POST("auth/verify-email")
    Call<PerfilResponse> verificarEmail(@Body Map<String, String> body);

    @POST("auth/resend-verification")
    Call<PerfilResponse> reenviarVerificacao(@Body Map<String, String> body);

    @POST("auth/googleConfig")
    Call<PerfilResponse> googleConfig();

    @POST("auth/loginGoogle")
    Call<LoginResponse> loginGoogle(@Body Map<String, String> body);

    // ---- Forgot password (no token needed) ----

    /** Body: email_usuario. The answer is the same whether or not the account exists. */
    @POST("auth/recuperarSenha")
    Call<PerfilResponse> recuperarSenha(@Body Map<String, String> body);

    /** Body: email_usuario, codigo and nova_senha. Signs the account out everywhere. */
    @POST("auth/redefinirSenha")
    Call<PerfilResponse> redefinirSenha(@Body Map<String, String> body);

    // ---- Connected devices (token needed) ----

    @POST("auth/sessoes")
    Call<SessoesResponse> listarSessoes();

    /** Body: id_sessao. */
    @POST("auth/sessaoEncerrar")
    Call<SessoesResponse> encerrarSessao(@Body Map<String, Object> corpo);

    /** Body: manter_atual (true keeps this device signed in). */
    @POST("auth/sessoesEncerrar")
    Call<SessoesResponse> encerrarSessoes(@Body Map<String, Object> corpo);

    /** Logout. Body: refresh_token. Works even after the access token expired. */
    @POST("auth/sair")
    Call<RespostaSimples> sair(@Body Map<String, String> corpo);

    // ---- Profile (token needed) ----

    @POST("auth/perfilObter")
    Call<PerfilResponse> perfilObter();

    @POST("auth/perfilAtualizar")
    Call<PerfilResponse> perfilAtualizar(@Body Map<String, String> body);

    @Multipart
    @POST("auth/perfilFoto")
    Call<PerfilResponse> perfilFoto(@Part MultipartBody.Part foto);

    @POST("auth/perfilSolicitarTrocaSenha")
    Call<PerfilResponse> perfilSolicitarTrocaSenha();

    @POST("auth/perfilConfirmarTrocaSenha")
    Call<PerfilResponse> perfilConfirmarTrocaSenha(@Body Map<String, String> body);

    @POST("auth/perfilSolicitarTrocaEmail")
    Call<PerfilResponse> perfilSolicitarTrocaEmail(@Body Map<String, String> body);

    @POST("auth/perfilAutorizarTrocaEmail")
    Call<PerfilResponse> perfilAutorizarTrocaEmail(@Body Map<String, String> body);

    @POST("auth/perfilConfirmarTrocaEmail")
    Call<PerfilResponse> perfilConfirmarTrocaEmail(@Body Map<String, String> body);

    @POST("aportes/registrar-aporte")
    Call<ApiResponse> registerAporte(@Body clsAportes aporte);

    @GET("aportes/buscar-aportes")
    Call<ApiResponse> getAportes(
            @Query("limite") Integer limite,
            @Query("id_carteira") int idCarteira
    );

    @GET("aportes/buscar-aportes")
    Call<AporteResponse> buscarAportes(@Query("id_carteira") int idCarteira);

    @GET("aportes/historico-aportes")
    Call<AporteResponse> buscarHistorico();

    @POST("aportes/listar-aportes")
    Call<ApiResponse> buscarLista(@Query("Choise") String escolha);

    @POST("carteiras/criar-carteira")
    Call<CriarWalletResponse> criarNovaWallet(@Body NovaCarteiraRequest carteira);

    @GET("carteiras/buscar-carteiras")
    Call<BuscarWalletsResponse> buscarWallets();

    @GET("brapi/get-quote")
    Call<QuoteResponse> getQuote(@Query("symbol") String symbol);

    @GET("brapi/get-symbols")
    Call<QuoteResponse> getService();

    @GET("brapi/listar-acoes-nacionais")
    Call<AcaoResponse> listarAcoesNacionais(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("search") String search,
            @Query("sector") String sector,
            @Query("subsector") String subsector,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder
    );

    @GET("brapi/listar-internacionais")
    Call<AcaoResponse> listarAtivosInternacionais(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("search") String search,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder
    );

    @GET("brapi/historico-acoes")
    Call<StockHistoryResponse> historicoAcoes(
            @Query("symbol") String symbol,
            @Query("range") String range
    );

    @GET("brapi/listar-fiis")
    Call<AcaoResponse> listarFIIs(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("search") String search,
            @Query("segmentType") String segmentType,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder
    );

    @GET("brapi/get-fii-quote")
    Call<QuoteResponse> getFiiQuote(@Query("symbol") String symbol);

    @GET("brapi/historico-fiis")
    Call<StockHistoryResponse> historicoFIIs(
            @Query("symbol") String symbol,
            @Query("range") String range
    );

    @GET("brapi/listar-futuros")
    Call<AcaoResponse> listarFuturos(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("search") String search,
            @Query("segment") String segment,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder
    );

    @GET("brapi/get-futures-quote")
    Call<QuoteResponse> getFutureQuote(@Query("symbol") String symbol);

    @GET("brapi/historico-futuros")
    Call<StockHistoryResponse> historicoFuturos(
            @Query("symbol") String symbol,
            @Query("range") String range
    );

    @GET("brapi/listar-criptomoedas")
    Call<AcaoResponse> listarCriptomoedas(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("search") String search,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder
    );

    @GET("brapi/get-crypto-quote")
    Call<QuoteResponse> getCryptoQuote(@Query("symbol") String symbol);

    @GET("brapi/historico-criptomoedas")
    Call<StockHistoryResponse> historicoCriptomoedas(
            @Query("symbol") String symbol,
            @Query("range") String range
    );

    @GET("favoritos/listar")
    Call<FavoritosResponse> listarFavoritos();

    @POST("favoritos/adicionar")
    Call<FavoritosResponse> adicionarFavorito(@Body Map<String, String> body);

    @POST("favoritos/remover")
    Call<FavoritosResponse> removerFavorito(@Body Map<String, String> body);

    @GET("brapi/listar-moedas")
    Call<AcaoResponse> listarMoedas(
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("search") String search,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder
    );

    @GET("brapi/get-currency-quote")
    Call<QuoteResponse> getCurrencyQuote(@Query("symbol") String symbol);

    @GET("brapi/historico-moedas")
    Call<StockHistoryResponse> historicoMoedas(
            @Query("symbol") String symbol,
            @Query("range") String range
    );

    @GET("carteiras/posicao-ativo")
    Call<AssetPositionResponse> getAssetPosition(
            @Query("symbol") String symbol
    );

    @GET("aportes/listar-titulosRF")
    Call<TitulosRendaFixaResponse> listarTitulosRendaFixa();

    @POST("carteiras/excluir-carteira")
    Call<RespostaSimples> excluirCarteira(
            @Query("id_carteira") int idCarteira
    );

    @POST("carteiras/adicionar-saldo")
    Call<AdicionarSaldoResponse> adicionarSaldo(
            @Query("valor")String valor
    );

    /** Current balance of the logged user (same payload shape as adicionar-saldo). */
    @GET("carteiras/saldo")
    Call<AdicionarSaldoResponse> buscarSaldo();

    // ---- Notificações ----

    @GET("notificacoes/listar")
    Call<NotificacoesResponse> listarNotificacoes(@Query("limite") int limite);

    @GET("notificacoes/contador")
    Call<NotificacoesResponse> contarNotificacoes();

    /** Corpo com "id_notificacao" marca uma; corpo vazio marca todas. */
    @POST("notificacoes/marcar-lidas")
    Call<NotificacoesResponse> marcarNotificacoesLidas(@Body Map<String, Object> corpo);

    // ---- Fale com a equipe ----

    @POST("auth/perguntaEnviar")
    Call<RespostaSimples> enviarPergunta(@Body Map<String, String> corpo);

    @POST("auth/perguntaListar")
    Call<PerguntasResponse> listarPerguntas();

    // ---- Patrimônio e simulações ----

    /** Saldo geral, totais (reais e simuladas separados) e cada carteira calculada. */
    @GET("carteiras/resumo")
    Call<ResumoPatrimonioResponse> resumoPatrimonio(@Query("id_carteira") Integer idCarteira);

    /** Corpo: id_carteira e valor. Só para carteiras simuladas. */
    @POST("carteiras/depositar-simulacao")
    Call<DepositoSimulacaoResponse> depositarSimulacao(@Body Map<String, Object> corpo);

    // ---- Notícias (públicas) ----

    @GET("noticias/listar")
    Call<NoticiasResponse> listarNoticias(@Query("limite") int limite);
}
