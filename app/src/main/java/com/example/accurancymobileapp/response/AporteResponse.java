package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.Aporte;

import java.util.ArrayList;

public class AporteResponse {
    private boolean sucesso;
    private int quantidade;
    private ArrayList<Aporte> aportes;

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
