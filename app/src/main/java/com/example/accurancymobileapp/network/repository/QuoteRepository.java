package com.example.accurancymobileapp.network.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.QuoteResult;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.QuoteResponse;
import com.example.accurancymobileapp.response.StockHistoryResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuoteRepository {

    private static final long ESPERA_NOVA_TENTATIVA_MS = 2500L;
    private static final String MENSAGEM_LIMITE_COTACOES =
            "Muitas consultas ao provedor de cotações. Aguarde alguns segundos e tente novamente.";

    private final ApiService apiService;

    public QuoteRepository(Context context) {
        apiService = RetrofitClient.getClient(context).create(ApiService.class);
    }

    public void buscarCotacoes(String ticker, QuoteCallback callback) {
        buscarCotacoes(ticker, "Ações", callback);
    }

    public void buscarCotacoes(String ticker, String categoria, QuoteCallback callback) {
        Call<QuoteResponse> chamada = ehCripto(categoria)
                ? apiService.getCryptoQuote(ticker)
                : ehFuturo(categoria)
                ? apiService.getFutureQuote(ticker)
                : ehFii(categoria)
                ? apiService.getFiiQuote(ticker)
                : apiService.getQuote(ticker);

        final boolean[] jaTentouNovamente = {false};
        chamada.enqueue(new Callback<QuoteResponse>() {
            @Override
            public void onResponse(Call<QuoteResponse> call, Response<QuoteResponse> response) {
                if (!response.isSuccessful()) {
                    int codigo = response.code();
                    if (codigo == 429) {
                        // Rate limit of the quote provider: wait a little and try one more time.
                        if (!jaTentouNovamente[0]) {
                            jaTentouNovamente[0] = true;
                            final Callback<QuoteResponse> esta = this;
                            new Handler(Looper.getMainLooper()).postDelayed(
                                    () -> call.clone().enqueue(esta), ESPERA_NOVA_TENTATIVA_MS);
                            return;
                        }
                        callback.onErro(MENSAGEM_LIMITE_COTACOES);
                        return;
                    }
                    if (ehFii(categoria) && (codigo == 403 || codigo == 404)) {
                        callback.onErro("A brapi não disponibilizou cotação para este FII (HTTP " + codigo + ").");
                    } else {
                        callback.onErro("Erro ao buscar a cotação. Código HTTP: " + codigo);
                    }
                    return;
                }

                QuoteResponse body = response.body();
                if (body == null || body.getResults() == null || body.getResults().isEmpty()) {
                    callback.onErro("A brapi não retornou cotação para " + ticker + ".");
                    return;
                }

                List<QuoteData> quotes = new ArrayList<>();
                for (QuoteResult result : body.getResults()) {
                    if (result == null) continue;

                    QuoteData quote = result.toQuoteData();
                    if (quote != null && quote.getRegularMarketPrice() != null
                            && Double.isFinite(quote.getRegularMarketPrice())
                            && quote.getRegularMarketPrice() > 0) {
                        quotes.add(quote);
                    }
                }

                if (quotes.isEmpty()) {
                    callback.onErro("A brapi não disponibilizou um preço válido para " + ticker + ".");
                    return;
                }
                callback.onSucesso(quotes);
            }

            @Override
            public void onFailure(Call<QuoteResponse> call, Throwable t) {
                callback.onErro(mensagemErro(t, "Não foi possível conectar ao servidor."));
            }
        });
    }

    public void buscarHistorico(String ticker, String range, HistoricoCallback callback) {
        buscarHistorico(ticker, range, "Ações", callback);
    }

    public void buscarHistorico(String ticker, String range, String categoria, HistoricoCallback callback) {
        Call<StockHistoryResponse> chamada = ehCripto(categoria)
                ? apiService.historicoCriptomoedas(ticker, range)
                : ehFuturo(categoria)
                ? apiService.historicoFuturos(ticker, range)
                : ehFii(categoria)
                ? apiService.historicoFIIs(ticker, range)
                : apiService.historicoAcoes(ticker, range);

        final boolean[] jaTentouNovamente = {false};
        chamada.enqueue(new Callback<StockHistoryResponse>() {
            @Override
            public void onResponse(Call<StockHistoryResponse> call, Response<StockHistoryResponse> response) {
                if (!response.isSuccessful()) {
                    int codigo = response.code();
                    if (codigo == 429) {
                        // Rate limit of the quote provider: wait a little and try one more time.
                        if (!jaTentouNovamente[0]) {
                            jaTentouNovamente[0] = true;
                            final Callback<StockHistoryResponse> esta = this;
                            new Handler(Looper.getMainLooper()).postDelayed(
                                    () -> call.clone().enqueue(esta), ESPERA_NOVA_TENTATIVA_MS);
                            return;
                        }
                        callback.onErro(MENSAGEM_LIMITE_COTACOES);
                        return;
                    }
                    if (ehFii(categoria) && (codigo == 403 || codigo == 404)) {
                        callback.onErro("Histórico diário indisponível para este FII (HTTP " + codigo + "). A cotação e a posição continuam independentes do gráfico.");
                    } else {
                        callback.onErro("Erro ao buscar histórico. Código HTTP: " + codigo);
                    }
                    return;
                }

                StockHistoryResponse body = response.body();
                if (body == null || body.getResults() == null || body.getResults().isEmpty()) {
                    callback.onSucesso(Collections.emptyList());
                    return;
                }

                StockHistoryResponse.Result first = body.getResults().get(0);
                if (first == null || first.getData() == null
                        || first.getData().getHistoricalDataPrice() == null) {
                    callback.onSucesso(Collections.emptyList());
                    return;
                }
                callback.onSucesso(first.getData().getHistoricalDataPrice());
            }

            @Override
            public void onFailure(Call<StockHistoryResponse> call, Throwable t) {
                callback.onErro(mensagemErro(t, "Não foi possível carregar o histórico de preços."));
            }
        });
    }

    private boolean ehFii(String categoria) {
        if (categoria == null) {
            return false;
        }

        String categoriaNormalizada = categoria.trim().toLowerCase(java.util.Locale.ROOT);
        return categoriaNormalizada.contains("fii")
                || categoriaNormalizada.contains("imobili");
    }

    private boolean ehCripto(String categoria) {
        return categoria != null
                && categoria.trim().toLowerCase(java.util.Locale.ROOT).startsWith("cripto");
    }

    private boolean ehFuturo(String categoria) {
        return categoria != null
                && categoria.trim().toLowerCase(java.util.Locale.ROOT).contains("futuro");
    }

    private String mensagemErro(Throwable t, String padrao) {
        return t.getMessage() == null || t.getMessage().trim().isEmpty() ? padrao : t.getMessage();
    }

    public interface QuoteCallback {
        void onSucesso(List<QuoteData> quotes);
        void onErro(String mensagem);
    }

    public interface HistoricoCallback {
        void onSucesso(List<StockHistoryResponse.Point> pontos);
        void onErro(String mensagem);
    }
}
