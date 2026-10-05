package com.example.accurancymobileapp.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.accurancymobileapp.BuildConfig;
import com.example.accurancymobileapp.activities.LoginActivity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;
    private final Context appContext;
    private static  volatile boolean redirecionando = false;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public AuthInterceptor(SessionManager sessionManager, Context context) {
        this.sessionManager = sessionManager;
        this.appContext = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain)
            throws IOException {

        String token = sessionManager.getToken();

        Request original = chain.request();

        if (deveIgnorarAutenticacao(original)) {

            return chain.proceed(original);
        }

        Request.Builder requestBuilder = original.newBuilder();

        if (token != null && !token.isEmpty()) {

            requestBuilder.header(
                    "Authorization",
                    "Bearer " + token
            );
        }

        Request request = requestBuilder.build();

        Log.d("AUTH", "URL: " + original.url());

        Log.d("AUTH", "Token existe: " + (token != null && !token.isEmpty()));

        Response response = chain.proceed(request);

        if (response.code() == 401) {

            Log.d("AUTH", "401 recebido. Tentando renovar token...");

            response.close();

            String newToken = renovarToken(token);

            if (newToken != null && !newToken.isEmpty()) {

                Log.d("AUTH", "Access Token renovado.");

                Request newRequest =
                        original.newBuilder()
                                .header(
                                        "Authorization",
                                        "Bearer " + newToken
                                )
                                .build();

                return chain.proceed(newRequest);
            }

            Log.d("AUTH", "Refresh Token inválido ou expirado.");

            forceLogout();

            return createUnauthorizedResponse(
                    original
            );
        }

        return response;
    }

    private synchronized String renovarToken(String tokenQueExpirou) {

        String tokenAtual = sessionManager.getToken();

        if (tokenAtual != null &&
                !tokenAtual.isEmpty() &&
                tokenQueExpirou != null &&
                !tokenQueExpirou.equals(tokenAtual)) {

            return tokenAtual;
        }

        String refreshToken = sessionManager.getRefreshToken();

        if (refreshToken == null || refreshToken.isEmpty()) {

            Log.d("AUTH", "Refresh Token não encontrado.");
            return null;
        }

        try {

            Gson gson = new Gson();

            JsonObject json = new JsonObject();

            json.addProperty(
                    "refresh_token",
                    refreshToken
            );

            RequestBody body = RequestBody.create(
                    json.toString(),
                    JSON
            );

            OkHttpClient refreshClient =
                    new OkHttpClient.Builder().build();

            String refreshUrl = chainUrlParaRefresh();

            Log.d("AUTH", "URL refresh: " + refreshUrl);

            Request refreshRequest =
                    new Request.Builder()
                            .url(refreshUrl)
                            .post(body)
                            .header("Content-Type", "application/json")
                            .build();

            try (Response refreshResponse =
                         refreshClient.newCall(refreshRequest).execute()) {

                String responseBody = "";

                if (refreshResponse.body() != null) {
                    responseBody = refreshResponse.body().string();
                }

                Log.d(
                        "AUTH",
                        "Refresh HTTP: " + refreshResponse.code()
                );

                Log.d(
                        "AUTH",
                        "Refresh response: " + responseBody
                );

                if (!refreshResponse.isSuccessful()) {

                    Log.e(
                            "AUTH",
                            "Falha no refresh HTTP: " +
                                    refreshResponse.code()
                    );

                    return null;
                }

                if (responseBody.isEmpty()) {

                    Log.e(
                            "AUTH",
                            "API retornou corpo vazio no refresh."
                    );

                    return null;
                }

                JsonElement elemento =
                        JsonParser.parseString(responseBody);

                if (!elemento.isJsonObject()) {

                    Log.e(
                            "AUTH",
                            "Resposta do refresh não é JsonObject: " +
                                    responseBody
                    );

                    return null;
                }

                JsonObject resposta =
                        elemento.getAsJsonObject();

                if (!resposta.has("success")) {

                    Log.e(
                            "AUTH",
                            "Resposta sem campo success: " +
                                    responseBody
                    );

                    return null;
                }

                boolean sucesso =
                        resposta.get("success").getAsBoolean();

                if (!sucesso) {

                    String mensagem =
                            resposta.has("message")
                                    ? resposta.get("message").getAsString()
                                    : "Mensagem não informada";

                    Log.e(
                            "AUTH",
                            "Refresh recusado pela API: " +
                                    mensagem
                    );

                    return null;
                }

                if (!resposta.has("token")) {

                    Log.e(
                            "AUTH",
                            "Refresh respondeu sucesso, mas não enviou token."
                    );

                    return null;
                }

                String novoToken =
                        resposta.get("token").getAsString();

                if (novoToken == null ||
                        novoToken.isEmpty()) {

                    Log.e(
                            "AUTH",
                            "Novo Access Token vazio."
                    );

                    return null;
                }

                sessionManager.saveToken(novoToken);

                Log.d(
                        "AUTH",
                        "Access Token renovado com sucesso."
                );

                return novoToken;
            }

        } catch (Exception e) {

            Log.e(
                    "AUTH",
                    "Erro ao renovar token",
                    e
            );

            return null;
        }
    }

    private String chainUrlParaRefresh() {

        return BuildConfig.API_URL + "auth/refresh";
    }

    private boolean deveIgnorarAutenticacao(Request request) {

        String path =
                request.url()
                        .encodedPath();

        return path.endsWith("auth/login") ||
                path.endsWith("auth/registrarNovoUsuario") ||
                path.endsWith("auth/refresh");
    }

    private Response createUnauthorizedResponse(Request request) {

        return new Response.Builder()
                .request(request)
                .protocol(
                        okhttp3.Protocol.HTTP_1_1
                )
                .code(401)
                .message("Sessão expirada")
                .body(
                        ResponseBody.create(
                                "",
                                MediaType.get(
                                        "application/json"
                                )
                        )
                )
                .build();
    }

    private synchronized void forceLogout() {

        if (redirecionando) {

            return;
        }

        redirecionando = true;

        sessionManager.logout();

        Intent intent =
                new Intent(
                        appContext,
                        LoginActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        appContext.startActivity(intent);

        redirecionando = false;
    }
}