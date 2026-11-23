package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.FavoritoAtivo;

import java.util.List;

public class FavoritosResponse {
    private boolean success;
    private String message;
    private Boolean favorito;
    private List<FavoritoAtivo> favoritos;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Boolean getFavorito() {
        return favorito;
    }

    public List<FavoritoAtivo> getFavoritos() {
        return favoritos;
    }
}
