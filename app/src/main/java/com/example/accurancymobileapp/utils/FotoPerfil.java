package com.example.accurancymobileapp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Profile photo helpers: shrink a picked image under the API limit, and load/cache the avatar. */
public final class FotoPerfil {

    /** The API accepts up to 2 MB; stay clearly below. */
    private static final int LIMITE_BYTES = 1_500_000;
    private static final int LADO_MAXIMO_ENVIO = 1024;
    private static final int LADO_MAXIMO_EXIBICAO = 512;
    private static final int LIMITE_DOWNLOAD = 6 * 1024 * 1024;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> CACHE =
            new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024 / 16)) {
                @Override
                protected int sizeOf(String chave, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };

    private FotoPerfil() {}

    public interface Resultado<T> {
        void aoConcluir(T valor);

        void aoFalhar(String mensagem);
    }

    // ---------------------------------------------------------------- sending

    /** Reads the picked image, fixes its rotation, shrinks it and returns a JPEG under the limit. */
    public static void prepararJpeg(@NonNull Context contexto, @NonNull Uri uri,
                                    @NonNull Resultado<byte[]> resultado) {
        final ContentResolver resolver = contexto.getApplicationContext().getContentResolver();
        EXECUTOR.execute(() -> {
            try {
                byte[] jpeg = gerarJpeg(resolver, uri);
                PRINCIPAL.post(() -> resultado.aoConcluir(jpeg));
            } catch (Throwable erro) {
                PRINCIPAL.post(() -> resultado.aoFalhar("Não foi possível ler essa imagem."));
            }
        });
    }

    private static byte[] gerarJpeg(ContentResolver resolver, Uri uri) throws Exception {
        BitmapFactory.Options limites = new BitmapFactory.Options();
        limites.inJustDecodeBounds = true;
        try (InputStream entrada = resolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(entrada, null, limites);
        }
        if (limites.outWidth <= 0 || limites.outHeight <= 0) throw new IllegalArgumentException("imagem");

        int amostra = 1;
        int maior = Math.max(limites.outWidth, limites.outHeight);
        while (maior / (amostra * 2) >= LADO_MAXIMO_ENVIO) amostra *= 2;

        BitmapFactory.Options opcoes = new BitmapFactory.Options();
        opcoes.inSampleSize = amostra;
        Bitmap original;
        try (InputStream entrada = resolver.openInputStream(uri)) {
            original = BitmapFactory.decodeStream(entrada, null, opcoes);
        }
        if (original == null) throw new IllegalArgumentException("imagem");

        int orientacao = ExifInterface.ORIENTATION_NORMAL;
        try (InputStream entrada = resolver.openInputStream(uri)) {
            if (entrada != null) {
                orientacao = new ExifInterface(entrada)
                        .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            }
        } catch (Exception ignorada) {
            // No EXIF data: keep the image as it is.
        }

        Matrix matriz = new Matrix();
        switch (orientacao) {
            case ExifInterface.ORIENTATION_ROTATE_90: matriz.postRotate(90); break;
            case ExifInterface.ORIENTATION_ROTATE_180: matriz.postRotate(180); break;
            case ExifInterface.ORIENTATION_ROTATE_270: matriz.postRotate(270); break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: matriz.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL: matriz.postScale(1, -1); break;
            default: break;
        }
        float escala = Math.min(1f, (float) LADO_MAXIMO_ENVIO
                / Math.max(original.getWidth(), original.getHeight()));
        if (escala < 1f) matriz.postScale(escala, escala);

        Bitmap ajustada = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matriz, true);
        // JPEG has no transparency: paint on white so a transparent PNG does not turn black.
        Bitmap fundoBranco = Bitmap.createBitmap(ajustada.getWidth(), ajustada.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tela = new Canvas(fundoBranco);
        tela.drawColor(Color.WHITE);
        tela.drawBitmap(ajustada, 0, 0, null);

        byte[] dados = null;
        for (int qualidade : new int[]{90, 80, 70, 60, 50, 40}) {
            ByteArrayOutputStream saida = new ByteArrayOutputStream();
            fundoBranco.compress(Bitmap.CompressFormat.JPEG, qualidade, saida);
            dados = saida.toByteArray();
            if (dados.length <= LIMITE_BYTES) break;
        }
        if (dados == null || dados.length > LIMITE_BYTES) throw new IllegalStateException("tamanho");
        return dados;
    }

    // ---------------------------------------------------------------- showing

    /** Decodes JPEG bytes into a small bitmap for the avatar. */
    @Nullable
    public static Bitmap decodificar(byte[] dados) {
        return amostrar(dados, LADO_MAXIMO_EXIBICAO);
    }

    public static void guardarNoCache(@Nullable String url, @Nullable Bitmap bitmap) {
        if (url != null && bitmap != null) CACHE.put(url, bitmap);
    }

    public static void limparCache() {
        CACHE.evictAll();
    }

    /** Loads the avatar (memory cache first). The listener always runs on the main thread. */
    public static void carregar(@NonNull String url, @NonNull Resultado<Bitmap> resultado) {
        Bitmap emCache = CACHE.get(url);
        if (emCache != null) {
            resultado.aoConcluir(emCache);
            return;
        }
        EXECUTOR.execute(() -> {
            HttpURLConnection conexao = null;
            try {
                conexao = (HttpURLConnection) new URL(url).openConnection();
                conexao.setConnectTimeout(15_000);
                conexao.setReadTimeout(15_000);
                if (conexao.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IllegalStateException("http");
                }
                ByteArrayOutputStream saida = new ByteArrayOutputStream();
                byte[] bloco = new byte[8192];
                try (InputStream entrada = conexao.getInputStream()) {
                    int lidos;
                    while ((lidos = entrada.read(bloco)) != -1) {
                        saida.write(bloco, 0, lidos);
                        if (saida.size() > LIMITE_DOWNLOAD) throw new IllegalStateException("grande");
                    }
                }
                Bitmap bitmap = amostrar(saida.toByteArray(), LADO_MAXIMO_EXIBICAO);
                if (bitmap == null) throw new IllegalStateException("imagem");
                CACHE.put(url, bitmap);
                PRINCIPAL.post(() -> resultado.aoConcluir(bitmap));
            } catch (Throwable erro) {
                PRINCIPAL.post(() -> resultado.aoFalhar("foto"));
            } finally {
                if (conexao != null) conexao.disconnect();
            }
        });
    }

    @Nullable
    private static Bitmap amostrar(byte[] dados, int ladoMaximo) {
        BitmapFactory.Options limites = new BitmapFactory.Options();
        limites.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(dados, 0, dados.length, limites);
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null;
        int amostra = 1;
        int maior = Math.max(limites.outWidth, limites.outHeight);
        while (maior / (amostra * 2) >= ladoMaximo) amostra *= 2;
        BitmapFactory.Options opcoes = new BitmapFactory.Options();
        opcoes.inSampleSize = amostra;
        return BitmapFactory.decodeByteArray(dados, 0, dados.length, opcoes);
    }
}
