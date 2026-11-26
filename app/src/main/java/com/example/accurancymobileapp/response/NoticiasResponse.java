package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.Noticia;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** GET /noticias/listar. */
public class NoticiasResponse extends RespostaSimples {

    @SerializedName("noticias")
    private List<Noticia> noticias;

    public List<Noticia> getNoticias() {
        return noticias != null ? noticias : new ArrayList<>();
    }
}
