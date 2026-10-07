package com.example.accurancymobileapp.network.repository;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.util.Log;

import com.example.accurancymobileapp.fragments.WalletFragment;
import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.AporteResponse;
import com.example.accurancymobileapp.response.QuoteResponse;
import com.example.accurancymobileapp.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AporteRepository {
    private ApiService apiService;

    public AporteRepository(Context context){
        Retrofit retrofit = RetrofitClient.getClient(context);

        apiService = retrofit.create(ApiService.class);
    }

    public void buscarAportes(int idCarteira, aporteCallback callback){

        Log.d("TESTE_APORTE", "ID carteira enviado: " + idCarteira);
        apiService.buscarAportes(idCarteira).enqueue(new Callback<AporteResponse>() {
            @Override
            public void onResponse(Call<AporteResponse> call, Response<AporteResponse> response) {

                if (!response.isSuccessful()) {
                    callback.onErro("Erro HTTP:" + response.code());
                    return;
                }
                AporteResponse resultado = response.body();

                if (resultado == null) {
                    callback.onErro("Resposta da API vazia: ");
                    return;
                }

                Log.d("TESTE_APORTE", "Sucesso: " + resultado.isSucesso());

                Log.d("TESTE_APORTE", "Quantidade: " + resultado.getQuantidade());

                if (resultado.getAtivos() == null) {

                    Log.e("TESTE_APORTE", "Lista = NULL");

                    callback.onErro("Lista de aportes vazia");
                    return;
                }

                callback.onSucesso(resultado.getAtivos());
            }

            @Override
            public void onFailure(Call<AporteResponse> call, Throwable t) {
                callback.onErro("Falha de conexão: " + t.getMessage());
            }
        });

    }


    public interface aporteCallback {
        void onSucesso(List<Aporte> aportes);

        void onErro(String mensagem);
    }
}
