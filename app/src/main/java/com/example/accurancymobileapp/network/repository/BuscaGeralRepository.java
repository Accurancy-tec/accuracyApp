package com.example.accurancymobileapp.network.repository;

import android.content.Context;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accurancymobileapp.model.Acao;
import com.example.accurancymobileapp.model.MarketType;
import com.example.accurancymobileapp.model.TituloRendaFixa;
import com.example.accurancymobileapp.response.AcaoResponse;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * One search over every kind of product: fixed income and Treasury titles (filtered here, the
 * list is small and cached) plus stocks, real-estate funds, BDRs/ETFs, crypto, currencies and
 * futures (each asks the API with its own search). Results come in as each category answers.
 */
public class BuscaGeralRepository {

    public static final int RESULTADOS_POR_CATEGORIA = 5;

    private static final long VALIDADE_MS = 3 * 60 * 1000L;
    private static final long VALIDADE_TITULOS_MS = 5 * 60 * 1000L;

    /** One group of results (a category). */
    public static final class Secao {
        public final String titulo;
        /** Null for the fixed income groups. */
        @Nullable
        public final MarketType mercado;
        /** For fixed income: true = Treasury titles, false = bank titles. */
        public final boolean tesouro;
        public final List<Acao> acoes = new ArrayList<>();
        public final List<TituloRendaFixa> titulos = new ArrayList<>();

        Secao(String titulo, @Nullable MarketType mercado, boolean tesouro) {
            this.titulo = titulo;
            this.mercado = mercado;
            this.tesouro = tesouro;
        }

        public boolean ehRendaFixa() {
            return mercado == null;
        }

        public int quantidade() {
            return ehRendaFixa() ? titulos.size() : acoes.size();
        }
    }

    public interface Ouvinte {
        /**
         * @param secoes     the categories that have results so far, in display order
         * @param carregando true while some category is still answering
         * @param falhas     how many categories could not be searched
         */
        void aoAtualizar(@NonNull List<Secao> secoes, boolean carregando, int falhas);
    }

    private static final class EntradaCache {
        final long quando = SystemClock.elapsedRealtime();
        final List<Acao> acoes;

        EntradaCache(List<Acao> acoes) {
            this.acoes = acoes;
        }
    }

    // Shared by every search so repeating or refining a search does not hit the API again.
    private static final Map<String, EntradaCache> CACHE = new HashMap<>();
    private static List<TituloRendaFixa> cacheTitulos;
    private static long cacheTitulosEm;

    private final MarketListRepository mercados;
    private final RendaFixaRepository rendaFixa;
    private int versao = 0;

    public BuscaGeralRepository(Context contexto) {
        mercados = new MarketListRepository(contexto);
        rendaFixa = new RendaFixaRepository(contexto);
    }

    public static void limparCache() {
        CACHE.clear();
        cacheTitulos = null;
    }

    /** Ignores anything still on its way from an earlier search. */
    public void cancelar() {
        versao++;
    }

    public void buscar(@NonNull String termoBruto, @NonNull Ouvinte ouvinte) {
        final String termo = termoBruto.trim();
        final int minhaVersao = ++versao;

        final Secao[] secoes = {
                new Secao("Renda fixa", null, false),
                new Secao("Tesouro Direto", null, true),
                new Secao("Ações", MarketType.ACOES_NACIONAIS, false),
                new Secao("Fundos imobiliários", MarketType.FIIS, false),
                new Secao("BDRs e ETFs", MarketType.BDRS_ETFS, false),
                new Secao("Criptomoedas", MarketType.CRIPTO, false),
                new Secao("Moedas estrangeiras", MarketType.MOEDAS, false),
                new Secao("Contratos futuros", MarketType.FUTUROS, false)
        };
        // Seven requests: fixed income (one list for both groups) + six markets.
        final int[] pendentes = {7};
        final int[] falhas = {0};

        final Runnable avisar = () -> {
            List<Secao> comResultado = new ArrayList<>();
            for (Secao s : secoes) {
                if (s.quantidade() > 0) comResultado.add(s);
            }
            ouvinte.aoAtualizar(comResultado, pendentes[0] > 0, falhas[0]);
        };

        // Fixed income and Treasury.
        obterTitulos(new RendaFixaRepository.RendaFixaCallback() {
            @Override
            public void onSuccess(List<TituloRendaFixa> titulos) {
                if (minhaVersao != versao) return;
                String procurado = normalizar(termo);
                for (TituloRendaFixa t : titulos) {
                    if (!combina(t, procurado)) continue;
                    Secao destino = ehTesouro(t) ? secoes[1] : secoes[0];
                    if (destino.titulos.size() < RESULTADOS_POR_CATEGORIA) destino.titulos.add(t);
                }
                pendentes[0]--;
                avisar.run();
            }

            @Override
            public void onError(String message) {
                if (minhaVersao != versao) return;
                falhas[0]++;
                pendentes[0]--;
                avisar.run();
            }
        });

        // Markets.
        for (int i = 2; i < secoes.length; i++) {
            final Secao secao = secoes[i];
            final MarketType tipo = secao.mercado;
            final String chave = tipo.getTag() + "|" + termo.toLowerCase(Locale.ROOT);
            EntradaCache guardada = CACHE.get(chave);
            if (guardada != null && SystemClock.elapsedRealtime() - guardada.quando < VALIDADE_MS) {
                secao.acoes.addAll(guardada.acoes);
                pendentes[0]--;
                continue;
            }
            mercados.listarAtivos(tipo, 1, RESULTADOS_POR_CATEGORIA, termo, null,
                    ordenacaoPadrao(tipo), tipo == MarketType.FUTUROS ? "asc" : "desc",
                    new MarketListRepository.MarketListCallback() {
                        @Override
                        public void onSuccess(AcaoResponse resultado) {
                            if (minhaVersao != versao) return;
                            List<Acao> lista = resultado == null || resultado.getAcoes() == null
                                    ? new ArrayList<>() : resultado.getAcoes();
                            if (lista.size() > RESULTADOS_POR_CATEGORIA) {
                                lista = new ArrayList<>(lista.subList(0, RESULTADOS_POR_CATEGORIA));
                            }
                            secao.acoes.addAll(lista);
                            CACHE.put(chave, new EntradaCache(new ArrayList<>(lista)));
                            pendentes[0]--;
                            avisar.run();
                        }

                        @Override
                        public void onError(String mensagem) {
                            if (minhaVersao != versao) return;
                            falhas[0]++;
                            pendentes[0]--;
                            avisar.run();
                        }
                    });
        }
        // Categories answered from the cache never call back: show what is already there.
        if (pendentes[0] < 7) avisar.run();
    }

    private void obterTitulos(RendaFixaRepository.RendaFixaCallback callback) {
        if (cacheTitulos != null && SystemClock.elapsedRealtime() - cacheTitulosEm < VALIDADE_TITULOS_MS) {
            callback.onSuccess(cacheTitulos);
            return;
        }
        rendaFixa.listarTitulosRendaFixa(new RendaFixaRepository.RendaFixaCallback() {
            @Override
            public void onSuccess(List<TituloRendaFixa> titulos) {
                cacheTitulos = new ArrayList<>(titulos);
                cacheTitulosEm = SystemClock.elapsedRealtime();
                callback.onSuccess(titulos);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private static String ordenacaoPadrao(MarketType tipo) {
        if (tipo == MarketType.FIIS) return "totalInvestors";
        if (tipo == MarketType.FUTUROS) return "expirationDate";
        return tipo.isCatalogoSimples() ? "rank" : "volume";
    }

    public static boolean ehTesouro(TituloRendaFixa titulo) {
        String tipo = titulo.getTipoTitulo();
        return tipo != null && tipo.trim().toLowerCase(Locale.ROOT).startsWith("tesouro");
    }

    /** Every word typed must show up in the name, type or indexer (accents and case ignored). */
    private static boolean combina(TituloRendaFixa titulo, String procurado) {
        if (procurado.isEmpty()) return false;
        String texto = normalizar(
                nz(titulo.getNomeTitulo()) + " " + nz(titulo.getTipoTitulo()) + " " + nz(titulo.getIndexador()));
        for (String palavra : procurado.split("\\s+")) {
            if (!texto.contains(palavra)) return false;
        }
        return true;
    }

    private static String nz(@Nullable String s) {
        return s == null ? "" : s;
    }

    static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
