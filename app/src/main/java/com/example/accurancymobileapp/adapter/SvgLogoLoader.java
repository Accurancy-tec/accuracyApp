package com.example.accurancymobileapp.adapter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import com.caverock.androidsvg.SVG;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class SvgLogoLoader {
    private static final ExecutorService executor =
            Executors.newFixedThreadPool(4);

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final int TAMANHO = 120;

    private static final LruCache<String, Bitmap> cache =
            new LruCache<String, Bitmap>(4 * 1024 * 1024) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount();
                }
            };

    public static void carregar(String url, ImageView imageView) {
        imageView.setTag(url);

        if (url == null || url.trim().isEmpty()) {
            imageView.setImageDrawable(null);
            return;
        }

        Bitmap emCache = cache.get(url);

        if (emCache != null) {
            imageView.setImageBitmap(emCache);
            return;
        }

        imageView.setImageDrawable(null);

        executor.execute(() -> {
            Bitmap bitmap = baixarSvg(url);

            if (bitmap != null) {
                cache.put(url, bitmap);
            }

            mainHandler.post(() -> {
                Object tagAtual = imageView.getTag();

                // Evita exibir o logo no item errado
                // quando o RecyclerView recicla a View.
                if (url.equals(tagAtual) && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            });
        });
    }

    private static Bitmap baixarSvg(String urlString) {
        HttpURLConnection conexao = null;

        try {
            URL url = new URL(urlString);

            conexao = (HttpURLConnection) url.openConnection();
            conexao.setConnectTimeout(10000);
            conexao.setReadTimeout(15000);
            conexao.setRequestMethod("GET");

            int status = conexao.getResponseCode();

            if (status < 200 || status >= 300) {
                return null;
            }

            try (InputStream input = conexao.getInputStream()) {
                SVG svg = SVG.getFromInputStream(input);

                // Obtém as dimensões originais do SVG
                float larguraOriginal = svg.getDocumentWidth();
                float alturaOriginal = svg.getDocumentHeight();

                // Usa uma proporção padrão se o SVG não informar dimensões
                if (larguraOriginal <= 0 || alturaOriginal <= 0) {
                    larguraOriginal = 100;
                    alturaOriginal = 100;
                }

                // Calcula o tamanho proporcional dentro de 120x120
                float escala = Math.min(
                        TAMANHO / larguraOriginal,
                        TAMANHO / alturaOriginal
                );

                int largura = Math.max(
                        1, Math.round(larguraOriginal * escala)
                );

                int altura = Math.max(
                        1, Math.round(alturaOriginal * escala)
                );

                Picture picture = svg.renderToPicture();

                largura = TAMANHO;
                altura = TAMANHO;

                Bitmap bitmap = Bitmap.createBitmap(
                        largura,
                        altura,
                        Bitmap.Config.ARGB_8888
                );

                Canvas canvas = new Canvas(bitmap);

                // Fundo transparente
                canvas.drawColor(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.PorterDuff.Mode.CLEAR
                );

                // Ajusta a escala mantendo a proporção
                float larguraSvg = picture.getWidth();
                float alturaSvg = picture.getHeight();

                if (larguraSvg > 0 && alturaSvg > 0) {
                    escala = Math.min(
                            (float) largura / larguraSvg,
                            (float) altura / alturaSvg
                    );

                    float deslocamentoX = (largura - larguraSvg * escala) / 2f;
                    float deslocamentoY = (altura - alturaSvg * escala) / 2f;

                    canvas.save();
                    canvas.translate(deslocamentoX, deslocamentoY);
                    canvas.scale(escala, escala);
                    canvas.drawPicture(picture);
                    canvas.restore();
                }

                return bitmap;
            }

        } catch (Exception e) {
            android.util.Log.e(
                    "SvgLogoLoader",
                    "Erro ao carregar logo: " + urlString,
                    e
            );

            return null;

        } finally {
            if (conexao != null) {
                conexao.disconnect();
            }
        }
    }
}
