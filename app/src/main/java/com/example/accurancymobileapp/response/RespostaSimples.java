package com.example.accurancymobileapp.response;

import com.google.gson.annotations.SerializedName;

/** Campos que toda resposta da API traz: se deu certo e, quando houver, uma mensagem. */
public class RespostaSimples {

    @SerializedName(value = "success", alternate = {"sucesso"})
    private Boolean success;

    @SerializedName(value = "message", alternate = {"mensagem"})
    private String message;

    public boolean ok() {
        return Boolean.TRUE.equals(success);
    }

    /** A mensagem do servidor ou, se ele não mandou nenhuma, o texto padrão. */
    public String texto(String padrao) {
        return message == null || message.trim().isEmpty() ? padrao : message;
    }
}
