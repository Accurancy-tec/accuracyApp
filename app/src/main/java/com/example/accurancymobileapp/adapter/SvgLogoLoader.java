package com.example.accurancymobileapp.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
        @Override protected int sizeOf(String key, Bitmap bitmap) { return bitmap.getByteCount(); }
    };

    private SvgLogoLoader() { }

    public static void carregar(String url, ImageView imageView) {
        if (imageView == null) return;
        String limpa = url == null ? null : url.trim();
        imageView.setTag(limpa);
        if (limpa == null || limpa.isEmpty()) return; // o adapter já definiu o placeholder

        Bitmap guardado = CACHE.get(limpa);
        if (guardado != null) {
            if (limpa.equals(imageView.getTag())) imageView.setImageBitmap(guardado);
            return;
        }

        EXECUTOR.execute(() -> {
            Bitmap bitmap = baixarImagem(limpa);
            if (bitmap != null) CACHE.put(limpa, bitmap);
            MAIN_HANDLER.post(() -> {
                if (limpa.equals(imageView.getTag()) && bitmap != null) imageView.setImageBitmap(bitmap);
            });
        });
    }

    private static Bitmap baixarImagem(String urlString) {
        HttpURLConnection conexao = null;
        try {
            URL url = new URL(urlString);
            String protocolo = url.getProtocol();
            if (!"https".equalsIgnoreCase(protocolo) && !"http".equalsIgnoreCase(protocolo)) return null;

            conexao = (HttpURLConnection) url.openConnection();
            conexao.setConnectTimeout(8000);
            conexao.setReadTimeout(12000);
            conexao.setRequestMethod("GET");
            conexao.setInstanceFollowRedirects(true);
            conexao.setRequestProperty("Accept", "image/svg+xml,image/*,*/*;q=0.8");

            int status = conexao.getResponseCode();
            if (status < 200 || status >= 300) {
                Log.w(TAG, "Logo indisponível HTTP " + status + ": " + urlString);
                return null;
            }

            byte[] bytes;
            try (InputStream input = conexao.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int lidos;
                while ((lidos = input.read(buffer)) != -1) output.write(buffer, 0, lidos);
                bytes = output.toByteArray();
            }
            if (bytes.length == 0) return null;

            Bitmap raster = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (raster != null) return ajustarTamanho(raster);

            SVG svg = SVG.getFromInputStream(new ByteArrayInputStream(bytes));
            Picture picture = svg.renderToPicture();
            int largura = picture.getWidth();
            int altura = picture.getHeight();
            if (largura <= 0 || altura <= 0) {
                Log.w(TAG, "SVG sem dimensões renderizáveis: " + urlString);
                return null;
            }

            Bitmap bitmap = Bitmap.createBitmap(TAMANHO, TAMANHO, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            float escala = Math.min((float) TAMANHO / largura, (float) TAMANHO / altura);
            float dx = (TAMANHO - largura * escala) / 2f;
            float dy = (TAMANHO - altura * escala) / 2f;
            canvas.save();
            canvas.translate(dx, dy);
            canvas.scale(escala, escala);
            canvas.drawPicture(picture);
            canvas.restore();
            return bitmap;
        } catch (Exception erro) {
            Log.w(TAG, "Falha ao carregar logo (placeholder será mantido): " + urlString, erro);
            return null;
        } finally {
            if (conexao != null) conexao.disconnect();
        }
    }

    private static Bitmap ajustarTamanho(Bitmap original) {
        int largura = original.getWidth();
        int altura = original.getHeight();
        if (largura <= 0 || altura <= 0) return original;
        float escala = Math.min((float) TAMANHO / largura, (float) TAMANHO / altura);
        int novaLargura = Math.max(1, Math.round(largura * escala));
        int novaAltura = Math.max(1, Math.round(altura * escala));
        if (novaLargura == largura && novaAltura == altura) return original;
        Bitmap ajustada = Bitmap.createScaledBitmap(original, novaLargura, novaAltura, true);
        if (ajustada != original) original.recycle();
        return ajustada;
    }
}
