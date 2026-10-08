package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.Aporte;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AporteResponse {
    private boolean sucesso;
    private int quantidade;
  
    @SerializedName(value = "ativos", alternate = {"aportes"})
    private ArrayList<Aporte> ativos;

    public boolean isSucesso() {
        return sucesso;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public ArrayList<Aporte> getAtivos() {
        return aportes;
    }
}
