package com.example.accurancymobileapp.network.repository;

import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.QuoteResult;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.QuoteResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuoteRepository {
    private ApiService apiService;

    public QuoteRepository(){
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    public void buscarCotacoes(String ticker, QuoteCallback callback){
        apiService.getQuote(ticker).enqueue(new Callback<QuoteResponse>() {
            @Override
            public void onResponse(Call<QuoteResponse> call, Response<QuoteResponse> response) {

                if(!response.isSuccessful()){
                    callback.onErro("Erro ao buscar a cotação. Código: " + response.code());
                    return;
                }

                QuoteResponse quoteResponse = response.body();

                if(quoteResponse.getResults() == null){
                    callback.onErro("A lista de resultado está vazia.");
                    return;
                }

                List<QuoteData> quotes = new ArrayList<>();

                for(QuoteResult result : quoteResponse.getResults()){
                    if(result != null && result.getData() != null){
                        quotes.add(result.getData());
                    }
                }

                callback.onSucesso(quotes);

            }

            @Override
            public void onFailure(Call<QuoteResponse> call, Throwable t) {
                String mensagem = t.getMessage();

                if(mensagem == null || mensagem.isEmpty()){
                    mensagem = "Não foi possível conectar ao servidor.";
                }

                callback.onErro(mensagem);
            }
        });
    }

    public interface QuoteCallback {
        void onSucesso(List<QuoteData> quotes);
        void onErro(String mensagem);
    }
}
