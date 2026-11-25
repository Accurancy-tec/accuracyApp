package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.PerguntaUsuario;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** Resposta de /auth/perguntaListar. */
public class PerguntasResponse extends RespostaSimples {

    @SerializedName("perguntas")
    private List<PerguntaUsuario> perguntas;

    public List<PerguntaUsuario> getPerguntas() {
        return perguntas == null ? new ArrayList<>() : perguntas;
    }
}
