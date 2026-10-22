package com.example.accurancymobileapp.adapter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.caverock.androidsvg.SVG;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SvgLogoLoader {
    private static final String TAG = "SvgLogoLoader";
    private static final int TAMANHO = 120;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(4 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount();
        }
    };

    private SvgLogoLoader() { }

    public static void carregar(String url, ImageView imageView) {
        if (imageView == null) return;
        imageView.setTag(url);
        if (url == null || url.trim().isEmpty()) {
            imageView.setImageDrawable(null);
            return;
        }

        Bitmap cached = CACHE.get(url);
        if (cached != null) {
            // O RecyclerView pode reutilizar o mesmo ImageView entre chamadas.
            if (url.equals(imageView.getTag())) imageView.setImageBitmap(cached);
            return;
        }

        imageView.setImageDrawable(null);
        EXECUTOR.execute(() -> {
            Bitmap bitmap = baixarSvg(url);
            if (bitmap != null) CACHE.put(url, bitmap);
            MAIN_HANDLER.post(() -> {
                if (url.equals(imageView.getTag()) && bitmap != null) imageView.setImageBitmap(bitmap);
            });
        });
    }

    private static Bitmap baixarSvg(String urlString) {
        HttpURLConnection conexao = null;
        try {
            URL url = new URL(urlString);
            if (!"https".equalsIgnoreCase(url.getProtocol()) && !"http".equalsIgnoreCase(url.getProtocol())) {
                return null;
            }
            conexao = (HttpURLConnection) url.openConnection();
            conexao.setConnectTimeout(10000);
            conexao.setReadTimeout(15000);
            conexao.setRequestMethod("GET");

            int status = conexao.getResponseCode();
            if (status < 200 || status >= 300) return null;

            try (InputStream input = conexao.getInputStream()) {
                SVG svg = SVG.getFromInputStream(input);
                Picture picture = svg.renderToPicture();

                int larguraPicture = picture.getWidth();
                int alturaPicture = picture.getHeight();
                if (larguraPicture <= 0 || alturaPicture <= 0) {
                    Log.w(TAG, "SVG sem dimensões renderizáveis: " + urlString);
                    return null;
                }

                Bitmap bitmap = Bitmap.createBitmap(TAMANHO, TAMANHO, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                float escala = Math.min(
                        (float) TAMANHO / larguraPicture,
                        (float) TAMANHO / alturaPicture
                );
                float deslocamentoX = (TAMANHO - larguraPicture * escala) / 2f;
                float deslocamentoY = (TAMANHO - alturaPicture * escala) / 2f;

                canvas.save();
                canvas.translate(deslocamentoX, deslocamentoY);
                canvas.scale(escala, escala);
                canvas.drawPicture(picture);
                canvas.restore();
                return bitmap;
            }
        } catch (Exception erro) {
            Log.e(TAG, "Erro ao carregar logo SVG: " + urlString, erro);
            return null;
        } finally {
            if (conexao != null) conexao.disconnect();
        }
    }
}
