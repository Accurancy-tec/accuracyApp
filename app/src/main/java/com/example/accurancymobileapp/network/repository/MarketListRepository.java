package com.example.accurancymobileapp.network.repository;

import android.content.Context;

import com.example.accurancymobileapp.model.MarketType;
import com.example.accurancymobileapp.response.AcaoResponse;

public class MarketListRepository {

    private final FIIsRepository fiisRepository;
    private final AcaoRepository acaoRepository;

    public MarketListRepository(Context context) {
        fiisRepository = new FIIsRepository(context);
        acaoRepository = new AcaoRepository(context);
    }

    public void listarAtivos(
            MarketType type,
            int pagina,
            int limite,
            MarketListCallback callback
    ) {

        if (type == null) {
            callback.onError("Tipo de mercado não informado.");
            return;
        }

        switch (type) {

            case FIIS:

                fiisRepository.listarFIIs(pagina, limite,
                        new FIIsRepository.FiiCallback() {

                            @Override
                            public void onSuccess(AcaoResponse resultado) {
                                callback.onSuccess(resultado);
                            }

                            @Override
                            public void onError(String mensagem) {
                                callback.onError(mensagem);
                            }
                        });

                break;

            case ACOES_NACIONAIS:

                acaoRepository.listarAcoesNacionais(pagina, limite,
                        new AcaoRepository.acaoCallback() {

                            @Override
                            public void onSuccess(AcaoResponse resultado) {
                                callback.onSuccess(resultado);
                            }

                            @Override
                            public void onError(String mensagem) {
                                callback.onError(mensagem);
                            }
                        });

                break;

            default:
                callback.onError("Tipo de mercado não suportado.");
                break;
        }
    }

    public interface MarketListCallback {

        void onSuccess(AcaoResponse resultado);

        void onError(String mensagem);
    }
}