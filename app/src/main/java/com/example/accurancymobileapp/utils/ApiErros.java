package com.example.accurancymobileapp.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.ResponseBody;

/** Reads the message the API puts in an error body ({message} or {mensagem}). */
public final class ApiErros {

    private ApiErros() {}

    public static String mensagem(ResponseBody corpo, String padrao) {
        if (corpo == null) return padrao;
        try {
            JsonElement elemento = JsonParser.parseString(corpo.string());
            if (!elemento.isJsonObject()) return padrao;
            JsonObject objeto = elemento.getAsJsonObject();
            for (String chave : new String[]{"message", "mensagem"}) {
                if (objeto.has(chave) && !objeto.get(chave).isJsonNull()) {
                    String texto = objeto.get(chave).getAsString();
                    if (!texto.trim().isEmpty()) return texto;
                }
            }
        } catch (Exception ignorada) {
            // Falls back to the default text below.
        }
        return padrao;
    }
}
